`include "defines.v"
`include "inst_def.v"

/****************************************************
*
* Automatically generated file; DO NOT EDIT.
*
*****************************************************/


module idu(
        input clk,
        input rst,

        //from ifu
        input [`InstDataBus] inst,
        input [`InstAddrBus] ifu_pc,

        //read regs (connect with gpr)
        output [`RegAddrBus] rs1,
        output [`RegAddrBus] rs2,
        input [`RegDataBus]  r_data1,
        input [`RegDataBus]  r_data2,

        //to exu
        output [`RegAddrBus] rd,
        output reg [7:0] inst_type,
        output reg [`RegDataBus] src1,
        output reg [`RegDataBus] src2,
        output reg [`RegDataBus] imm,
        output reg [`InstAddrBus] idu_pc,
        output [`InstDataBus] idu_inst
);

wire [6:0]opcode = inst[6:0];
wire [2:0]funct3 = inst[14:12];
wire [6:0]funct7 = inst[31:25];

assign rs1 = inst[19:15];
assign rs2 = inst[24:20];
assign rd = inst[11:7];
assign idu_inst = inst;

wire [`WordBus] immI = { {(`WordWidth-12){inst[31]}}, inst[31:20] };
wire [`WordBus] immU = { inst[31:12], {12{1'b0}} };
wire [`WordBus] immS = { {(`WordWidth-12){inst[31]}}, inst[31:25], inst[11:7] };
wire [`WordBus] immJ = { {(`WordWidth-21){inst[31]}}, inst[31], inst[19:12], inst[20], inst[30:21],1'b0 };
wire [`WordBus] immB = { {(`WordWidth-13){inst[31]}}, inst[31], inst[7], inst[30:25], inst[11:8], 1'b0};



always @(*) begin
        idu_pc = ifu_pc;
end

assign src1 = r_data1;
assign src2 = r_data2;



