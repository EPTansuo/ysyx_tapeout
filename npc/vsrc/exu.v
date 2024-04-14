`include "defines.v"
`include "inst_def.v"


module exu(
        input clk,
        input rst,

        //从IDU传来的数据
        input [`RegDataBus] src1,
        input [`RegDataBus] src2,
        input [`RegDataBus] imm,
        input [7:0] inst_type,
        input [`RegAddrBus] rd,
        input [`InstAddrBus] idu_pc,

        //写入寄存器
        output reg [`RegDataBus] w_data,
        output reg [`RegAddrBus] w_addr,
        output reg we,

        //传输到PC
        output reg [`InstAddrBus] pc_offset,
        output reg pc_offset_en
);


wire [`WordBus] add_src1_imm;

assign add_src1_imm = src1+imm;


always @(*) begin
        case (inst_type)
                `Inst_addi: begin
                        w_data = add_src1_imm;
                        w_addr = rd;
                        we = `WriteEnable;
                        pc_offset_en = `Disable;
                end
                `Inst_auipc: begin
                        w_data = add_src1_imm + idu_pc; //src1被设为0，则aupic可重复利用addi的加法器
                        w_addr = rd;
                        we = `WriteEnable;
                        pc_offset_en = `Disable;
                        pc_offset_en = `Disable;
                end
                `Inst_lui: begin
                        w_data = imm;
                        w_addr = rd;
                        we = `WriteEnable;
                        pc_offset_en = `Disable;
                end
                `Inst_jal: begin
                        w_data = idu_pc + 4;
                        w_addr = rd;
                        we = `WriteEnable;
                        pc_offset_en = `Enable;
                        pc_offset = imm;
                end
                `Inst_jalr: begin
                        w_data = idu_pc + 4;
                        w_addr = rd;
                        we = `WriteEnable;
                        pc_offset_en = `Enable;
                        pc_offset = {add_src1_imm[`WordWidth-1:1], 1'b0};
                end
                default:begin
                        w_data = 0;
                        w_addr = 0;
                        pc_offset_en = `Disable;
                        we = `WriteDisable;
                end
        endcase
end


endmodule

