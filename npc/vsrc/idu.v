`include "defines.v"
`include "inst_def.v"


module idu(
        input clk,
        input rst,

        //从IFU读取的指令
        input [`InstDataBus] inst,
        input [`InstAddrBus] ifu_pc,

        //读取寄存器
        output [`RegAddrBus] rs1,
        output [`RegAddrBus] rs2,
        input [`RegDataBus]  r_data1,
        input [`RegDataBus]  r_data2,

        //输出到EXU
        output [`RegAddrBus] rd,
        output reg [7:0] inst_type,
        output reg [`RegDataBus] src1,
        output reg [`RegDataBus] src2,
        output reg [`RegDataBus] imm,
        output reg [`InstAddrBus] idu_pc
);

wire [6:0]opcode = inst[6:0];
wire [2:0]funct3 = inst[14:12];
wire [6:0]funct7 = inst[31:25];

assign rs1 = inst[19:15];
assign rs2 = inst[24:20];
assign rd = inst[11:7];

wire [`WordBus] immI = { {(`WordWidth-12){inst[31]}}, inst[31:20] };
wire [`WordBus] immU = { inst[31:12], {12{1'b0}} };
wire [`WordBus] immS = { {(`WordWidth-12){inst[31]}}, inst[31:25], inst[11:7] };
wire [`WordBus] immJ = { {(`WordWidth-21){inst[31]}}, inst[31], inst[19:12], inst[20], inst[30:21],1'b0 };
wire [`WordBus] immB = { {(`WordWidth-13){inst[31]}}, inst[31], inst[7], inst[30:25], inst[11:8], 1'b0};



always @(*) begin
        idu_pc = ifu_pc;
end


always @(*)begin
        case(opcode)
                `OP_I_TYPE:begin
                        src1 = r_data1;
                        src2 = 0;
                        imm = immI;
                        case(funct3)
                                `Funct3_addi:begin
                                        inst_type = `Inst_addi;
                                end
                                3'b011: begin
                                        inst_type = `Inst_sltiu;
                                end
                                default:begin
                                        inst_type = `Inst_inv;
                                end
                        endcase
                end
                `OP_I_TYPE_L: begin
                        src1 = r_data1;
                        src2 = 0;
                        imm = immI;
                        case(funct3)
                                3'b010: begin
                                        inst_type = `Inst_lw;
                                end
                                default: begin
                                        inst_type = `Inst_inv;
                                end
                        endcase
                end
                `OP_S_TYPE: begin
                        src1 = r_data1;
                        src2 = r_data2;
                        imm = immS;
                        case (funct3)
                                `Funct3_sw:begin
                                        inst_type = `Inst_sw;
                        end 
                                default: begin
                                        inst_type = `Inst_inv;
                                end
                        endcase
                end

                `OP_J_TYPE: begin
                        src1 = 0;
                        src2 = 0;
                        inst_type = `Inst_jal;
                        imm = immJ;
                end

                `OP_B_TYPE: begin
                        imm = immB;
                        src1 = r_data1;
                        src2 = r_data2;
                        case(funct3)
                                3'b000: begin
                                        inst_type = `Inst_beq;
                                end
                                3'b001: begin
                                        inst_type = `Inst_bne;
                                end
                                3'b110: begin
                                        inst_type = `Inst_blt;
                                end
                                default: begin
                                        inst_type = `Inst_inv;
                                end
                        endcase
                end

                `OP_R_TYPE: begin
                        src1 = r_data1;
                        src2 = r_data2;
                        imm = 0;
                        case(funct7) 
                                7'b000_0000: begin
                                        case(funct3)
                                                3'b000: begin
                                                        inst_type = `Inst_add;
                                                end
                                                default: begin
                                                        inst_type = `Inst_inv;
                                                end
                                        endcase
                                end
								7'b010_0000: begin
										case(funct3) 
												3'b000: begin
														inst_type = `Inst_sub;
												end
												default: begin
														inst_type = `Inst_inv;
												end
										endcase
								end
                                default: begin
                                		inst_type = `Inst_inv;        
                                end
                        endcase
                end
                `OP_U_TYPE_aupic: begin
                        inst_type = `Inst_auipc;
                        src1 = ifu_pc;   //将src1设为pc，则aupic可重复利用addi的加法器
                        src2 = 0;
                        imm = immU;
                end
                `OP_U_TYPE_lui:begin
                        inst_type = `Inst_lui;
                        imm = immU;
                        src1 = 0;
                        src2 = 0;
                end
                `OP_I_TYPE_jarl:begin
                        inst_type = `Inst_jalr;
                        src1 = r_data1;
                        src2 = 0;
                        imm = immI;
                end
                default:begin
                        if( inst == `EBREAK ) begin
                                inst_type = `Inst_ebreak;
                                src1 = 0;
                                src2 = 0;
                                imm = 0;
                        end
                        else begin
                                inst_type = `Inst_inv;
                                src1 = 0;
                                src2 = 0;
                                imm = 0;
                        end

                end
        endcase
end



endmodule
