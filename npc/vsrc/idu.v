`include "defines.v"

module idu(
        input clk,
        input rst,

        //从IFU读取的指令
        input [`InstDataBus] inst,

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
        output reg [`RegDataBus] imm
);

wire [6:0]opcode = inst[6:0];
wire [2:0]funct3 = inst[14:12];
wire [6:0]funct7 = inst[31:25];

assign rs1 = inst[19:15];
assign rs2 = inst[24:20];
assign rd = inst[11:7];

wire [`WordBus] immI = { {(`WordWidth-12){inst[31]}}, inst[31:20] };



import "DPI-C" function void npc_ebreak();

always @(*) begin
        if(inst == `EBREAK)begin
                npc_ebreak();
        end
end


always @(*)begin
        case(opcode)
                `OP_I_TYPE:begin
                        inst_type = `Inst_addi;
                        src1 = r_data1;
                        src2 = r_data2;
                        imm = immI;
                end
                default:begin
                        inst_type = 8'b0;
                        src1 = 0;
                        src2 = 0;
                        imm = 0;
                end
        endcase
end



endmodule