`include "defines.v"

module imm(
        input wire [`InstDataBus] inst,
        input wire [2:0] imm_type,
        output reg [`RegBus] imm_out
);

//这里只有addi，是I型的指令
assign imm_out = { { 20{inst[31]} }, inst[31:20] }; //取立即数，并做符号扩展


endmodule