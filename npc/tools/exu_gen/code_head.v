`include "defines.v"
`include "inst_def.v"

/****************************************************
*
* Automatically generated file; DO NOT EDIT.
*
*****************************************************/

module exu(
        input clk,
        input rst,

        //from IDU
        input [`RegDataBus] src1,
        input [`RegDataBus] src2,
        input [`RegDataBus] imm,
        input [7:0] inst_type,
        input [`RegAddrBus] rd,
        input [`InstAddrBus] idu_pc,
        input [`InstDataBus] idu_inst,

        //to gpr
        output reg [`RegDataBus] gpr_w_data,
        output reg [`RegAddrBus] gpr_w_addr,
        output reg gpr_we,

        //to PC
        output reg [`InstAddrBus] pc_offset,
        output reg pc_offset_en,

        //to MEM
        output reg mem_we,
        output reg mem_re,
        output reg [`InstAddrBus] mem_w_addr,
        output reg [`InstAddrBus] mem_r_addr,
        output reg [`WordBus] mem_w_data,
        output reg [7:0] mem_w_mask,
        output reg [7:0] mem_r_mask,

        //from MEM
        input wire [`WordBus] mem_r_data,


        //from pc
        input wire [`InstAddrBus] exu_pc,

        //from csr
        input  [`RegDataBus] csr_rdata,

        //to csr
        output reg [`InstAddrBus] exu_csr_pc,
        output reg [7:0] exu_csr_inst_type,
        output reg [11:0] csr_raddr,
        output reg [`RegDataBus] csr_wdata,
        output reg [11:0] csr_waddr,
        output reg csr_we
);

assign exu_csr_pc = exu_pc;

`ifndef STA

import "DPI-C" function void npc_ebreak();
import "DPI-C" function void inst_invalid();

reg exu_invalid_inst;


always @(*) begin
        if(inst_type == `Inst_ebreak)begin
                npc_ebreak();
        end
end

always @(*) begin
        if(idu_pc >= `Init_Addr && (inst_type == `Inst_inv || exu_invalid_inst)) begin
                inst_invalid();
        end
end

`endif

wire [`WordBus] add_src1_imm;
wire [4:0] shamt;
wire [`SHAMT_LONG_LEN-1:0] shamt_long;
wire [`WordBus] sra_src1_sranum;

assign add_src1_imm = src1+imm;
assign shamt = `SHAMT;
assign shamt_long = `SHAMT_LONG;

`ifdef CONFIG_RV32
reg [4:0] sranum;
assign sra_src1_sranum = ({32{src1[31]}} << (6'd32-{1'b0,sranum[4:0]})) | (src1 >> sranum[4:0]);
`endif

