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
        input wire [`WordBus] mem_r_data
);

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
assign add_src1_imm = src1+imm;

`ifndef STA
assign exu_invalid_inst = rst == `RstEnable ? 0 : 
                          inst_type == `Inst_ebreak ? 0 : 
                          inst_type == `Inst_addi ? 0 : 
                          inst_type == `Inst_auipc ? 0 : 
                          inst_type == `Inst_jal ? 0 : 
                          1;
`endif


assign gpr_we = inst_type == `Inst_addi ? `Enable : 
                inst_type == `Inst_auipc ? `Enable : 
                inst_type == `Inst_jal ? `Enable : 
                `Disable;


assign gpr_w_addr = inst_type == `Inst_addi ? rd : 
                    inst_type == `Inst_auipc ? rd : 
                    inst_type == `Inst_jal ? rd : 
                    0;


assign gpr_w_data = inst_type == `Inst_addi ? add_src1_imm : 
                    inst_type == `Inst_auipc ? idu_pc+imm : 
                    inst_type == `Inst_jal ? idu_pc+4 : 
                    0;


assign pc_offset_en = inst_type == `Inst_addi ? `Disable : 
                      inst_type == `Inst_auipc ? `Disable : 
                      inst_type == `Inst_jal ? `Enable : 
                      `Disable;


assign pc_offset = inst_type == `Inst_addi ? 4 : 
                   inst_type == `Inst_auipc ? 4 : 
                   inst_type == `Inst_jal ? imm : 
                   4;


assign mem_re = inst_type == `Inst_addi ? `Disable : 
                inst_type == `Inst_auipc ? `Disable : 
                inst_type == `Inst_jal ? `Disable : 
                `Disable;


assign mem_r_addr = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    0;


assign mem_r_mask = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    0;


assign mem_we = inst_type == `Inst_addi ? `Disable : 
                inst_type == `Inst_auipc ? `Disable : 
                inst_type == `Inst_jal ? `Disable : 
                `Disable;


assign mem_w_addr = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    0;


assign mem_w_data = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    0;


assign mem_w_mask = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    0;



endmodule


