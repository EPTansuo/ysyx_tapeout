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
        output reg we
);

wire [`WordBus] add_src1_imm;

assign add_src1_imm = src1+imm;


always @(*) begin
        case (inst_type)
                `Inst_addi:begin
                        w_data = add_src1_imm;
                        w_addr = rd;
                        we = `WriteEnable;
                end

                default:begin
                        w_data = 0;
                        w_addr = 0;
                        we = `WriteDisable;
                end
        endcase
end


endmodule

