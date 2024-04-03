`include "defines.v"

module idu(
        input wire clk,
        input wire rst,

        input wire [`InstDataBus] inst,

        output reg [2:0] imm_type,
        output reg reg_we
);
reg [6:0]opcode;
reg [2:0]funct3;
reg [6:0]funct7;

assign reg_we = `WriteEnable;
assign imm_type = `TypeI;



endmodule