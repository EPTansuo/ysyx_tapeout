
`include "defines.v"

module alu(
        input wire clk,
        input wire rst,

        input wire[`AluSelBus] alusel,
        input wire[`RegBus] src1, //ALU源操作数1
        input wire[`RegBus] src2, //ALU源操作数2
        output reg[`RegBus] out, //ALU输出
        output reg zero //零标志位

);
        wire [`RegBus] result_sum;

        assign result_sum =  src1 + src2;

        // Muxkey #(4, 3, 32) alumux (
 
        // );


endmodule