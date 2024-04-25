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

`ifndef STA
assign exu_invalid_inst = rst == `RstEnable ? 0 : 
                          inst_type == `Inst_ebreak ? 0 : 
                          inst_type == `Inst_addi ? 0 : 
                          inst_type == `Inst_andi ? 0 : 
                          inst_type == `Inst_auipc ? 0 : 
                          inst_type == `Inst_lui ? 0 : 
                          inst_type == `Inst_jal ? 0 : 
                          inst_type == `Inst_jalr ? 0 : 
                          inst_type == `Inst_beq ? 0 : 
                          inst_type == `Inst_bne ? 0 : 
                          inst_type == `Inst_bgeu ? 0 : 
                          inst_type == `Inst_bltu ? 0 : 
                          inst_type == `Inst_blt ? 0 : 
                          inst_type == `Inst_bge ? 0 : 
                          inst_type == `Inst_or ? 0 : 
                          inst_type == `Inst_xor ? 0 : 
                          inst_type == `Inst_sltu ? 0 : 
                          inst_type == `Inst_sltiu ? 0 : 
                          inst_type == `Inst_sub ? 0 : 
                          inst_type == `Inst_add ? 0 : 
                          inst_type == `Inst_srl ? 0 : 
                          inst_type == `Inst_srai ? 0 : 
                          inst_type == `Inst_sra ? 0 : 
                          inst_type == `Inst_srli ? 0 : 
                          inst_type == `Inst_slli ? 0 : 
                          inst_type == `Inst_lbu ? 0 : 
                          inst_type == `Inst_lw ? 0 : 
                          inst_type == `Inst_sh ? 0 : 
                          inst_type == `Inst_sw ? 0 : 
                          1;
`endif


assign gpr_we = inst_type == `Inst_addi ? `Enable : 
                inst_type == `Inst_andi ? `Enable : 
                inst_type == `Inst_auipc ? `Enable : 
                inst_type == `Inst_lui ? `Enable : 
                inst_type == `Inst_jal ? `Enable : 
                inst_type == `Inst_jalr ? `Enable : 
                inst_type == `Inst_beq ? `Disable : 
                inst_type == `Inst_bne ? `Disable : 
                inst_type == `Inst_bgeu ? `Disable : 
                inst_type == `Inst_bltu ? `Disable : 
                inst_type == `Inst_blt ? `Disable : 
                inst_type == `Inst_bge ? `Disable : 
                inst_type == `Inst_or ? `Enable : 
                inst_type == `Inst_xor ? `Enable : 
                inst_type == `Inst_sltu ? `Enable : 
                inst_type == `Inst_sltiu ? `Enable : 
                inst_type == `Inst_sub ? `Enable : 
                inst_type == `Inst_add ? `Enable : 
                inst_type == `Inst_srl ? `Enable : 
                inst_type == `Inst_srai ? `Enable : 
                inst_type == `Inst_sra ? `Enable : 
                inst_type == `Inst_srli ? `Enable : 
                inst_type == `Inst_slli ? `Enable : 
                inst_type == `Inst_lbu ? `Enable : 
                inst_type == `Inst_lw ? `Enable : 
                inst_type == `Inst_sh ? `Disable : 
                inst_type == `Inst_sw ? `Disable : 
                `Disable;


assign gpr_w_addr = inst_type == `Inst_addi ? rd : 
                    inst_type == `Inst_andi ? rd : 
                    inst_type == `Inst_auipc ? rd : 
                    inst_type == `Inst_lui ? rd : 
                    inst_type == `Inst_jal ? rd : 
                    inst_type == `Inst_jalr ? rd : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? rd : 
                    inst_type == `Inst_xor ? rd : 
                    inst_type == `Inst_sltu ? rd : 
                    inst_type == `Inst_sltiu ? rd : 
                    inst_type == `Inst_sub ? rd : 
                    inst_type == `Inst_add ? rd : 
                    inst_type == `Inst_srl ? rd : 
                    inst_type == `Inst_srai ? rd : 
                    inst_type == `Inst_sra ? rd : 
                    inst_type == `Inst_srli ? rd : 
                    inst_type == `Inst_slli ? rd : 
                    inst_type == `Inst_lbu ? rd : 
                    inst_type == `Inst_lw ? rd : 
                    inst_type == `Inst_sh ? 0 : 
                    inst_type == `Inst_sw ? 0 : 
                    0;


assign gpr_w_data = inst_type == `Inst_addi ? add_src1_imm : 
                    inst_type == `Inst_andi ? src1 & imm : 
                    inst_type == `Inst_auipc ? idu_pc+imm : 
                    inst_type == `Inst_lui ? imm : 
                    inst_type == `Inst_jal ? idu_pc+4 : 
                    inst_type == `Inst_jalr ? idu_pc+4 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? src1 | src2 : 
                    inst_type == `Inst_xor ? src1 ^ src2 : 
                    inst_type == `Inst_sltu ? src1 < src2 ? 1 : 0 : 
                    inst_type == `Inst_sltiu ? src1 < imm ? 1 : 0 : 
                    inst_type == `Inst_sub ? src1 - src2 : 
                    inst_type == `Inst_add ? src1 + src2 : 
                    inst_type == `Inst_srl ? src1 >> src2 : 
                    inst_type == `Inst_srai ? sra_src1_sranum : 
                    inst_type == `Inst_sra ? sra_src1_sranum : 
                    inst_type == `Inst_srli ? src1 >> shamt_long : 
                    inst_type == `Inst_slli ? src1 << shamt_long : 
                    inst_type == `Inst_lbu ? mem_r_data : 
                    inst_type == `Inst_lw ? mem_r_data : 
                    inst_type == `Inst_sh ? 0 : 
                    inst_type == `Inst_sw ? 0 : 
                    0;


assign pc_offset_en = inst_type == `Inst_addi ? `Disable : 
                      inst_type == `Inst_andi ? `Disable : 
                      inst_type == `Inst_auipc ? `Disable : 
                      inst_type == `Inst_lui ? `Disable : 
                      inst_type == `Inst_jal ? `Enable : 
                      inst_type == `Inst_jalr ? `Enable : 
                      inst_type == `Inst_beq ? `Enable : 
                      inst_type == `Inst_bne ? `Enable : 
                      inst_type == `Inst_bgeu ? `Enable : 
                      inst_type == `Inst_bltu ? `Enable : 
                      inst_type == `Inst_blt ? `Enable : 
                      inst_type == `Inst_bge ? `Enable : 
                      inst_type == `Inst_or ? `Disable : 
                      inst_type == `Inst_xor ? `Disable : 
                      inst_type == `Inst_sltu ? `Disable : 
                      inst_type == `Inst_sltiu ? `Disable : 
                      inst_type == `Inst_sub ? `Disable : 
                      inst_type == `Inst_add ? `Disable : 
                      inst_type == `Inst_srl ? `Disable : 
                      inst_type == `Inst_srai ? `Disable : 
                      inst_type == `Inst_sra ? `Disable : 
                      inst_type == `Inst_srli ? `Disable : 
                      inst_type == `Inst_slli ? `Disable : 
                      inst_type == `Inst_lbu ? `Disable : 
                      inst_type == `Inst_lw ? `Disable : 
                      inst_type == `Inst_sh ? `Disable : 
                      inst_type == `Inst_sw ? `Disable : 
                      `Disable;


assign pc_offset = inst_type == `Inst_addi ? 4 : 
                   inst_type == `Inst_andi ? 4 : 
                   inst_type == `Inst_auipc ? 4 : 
                   inst_type == `Inst_lui ? 4 : 
                   inst_type == `Inst_jal ? imm : 
                   inst_type == `Inst_jalr ? {add_src1_imm[`WordWidth-1:1],1'b0} - idu_pc : 
                   inst_type == `Inst_beq ? src1 == src2 ? imm : 4 : 
                   inst_type == `Inst_bne ? src1 != src2 ? imm : 4 : 
                   inst_type == `Inst_bgeu ? src1 >= src2 ? imm : 4 : 
                   inst_type == `Inst_bltu ? src1 < src2 ? imm : 4 : 
                   inst_type == `Inst_blt ? $signed(src1) < $signed(src2) ? imm : 4 : 
                   inst_type == `Inst_bge ? $signed(src1) >= $signed(src2) ? imm : 4 : 
                   inst_type == `Inst_or ? 4 : 
                   inst_type == `Inst_xor ? 4 : 
                   inst_type == `Inst_sltu ? 4 : 
                   inst_type == `Inst_sltiu ? 4 : 
                   inst_type == `Inst_sub ? 4 : 
                   inst_type == `Inst_add ? 4 : 
                   inst_type == `Inst_srl ? 4 : 
                   inst_type == `Inst_srai ? 4 : 
                   inst_type == `Inst_sra ? 4 : 
                   inst_type == `Inst_srli ? 4 : 
                   inst_type == `Inst_slli ? 4 : 
                   inst_type == `Inst_lbu ? 4 : 
                   inst_type == `Inst_lw ? 4 : 
                   inst_type == `Inst_sh ? 4 : 
                   inst_type == `Inst_sw ? 4 : 
                   4;


assign mem_re = inst_type == `Inst_addi ? `Disable : 
                inst_type == `Inst_andi ? `Disable : 
                inst_type == `Inst_auipc ? `Disable : 
                inst_type == `Inst_lui ? `Disable : 
                inst_type == `Inst_jal ? `Disable : 
                inst_type == `Inst_jalr ? `Disable : 
                inst_type == `Inst_beq ? `Disable : 
                inst_type == `Inst_bne ? `Disable : 
                inst_type == `Inst_bgeu ? `Disable : 
                inst_type == `Inst_bltu ? `Disable : 
                inst_type == `Inst_blt ? `Disable : 
                inst_type == `Inst_bge ? `Disable : 
                inst_type == `Inst_or ? `Disable : 
                inst_type == `Inst_xor ? `Disable : 
                inst_type == `Inst_sltu ? `Disable : 
                inst_type == `Inst_sltiu ? `Disable : 
                inst_type == `Inst_sub ? `Disable : 
                inst_type == `Inst_add ? `Disable : 
                inst_type == `Inst_srl ? `Disable : 
                inst_type == `Inst_srai ? `Disable : 
                inst_type == `Inst_sra ? `Disable : 
                inst_type == `Inst_srli ? `Disable : 
                inst_type == `Inst_slli ? `Disable : 
                inst_type == `Inst_lbu ? `Enable : 
                inst_type == `Inst_lw ? `Enable : 
                inst_type == `Inst_sh ? `Disable : 
                inst_type == `Inst_sw ? `Disable : 
                `Disable;


assign mem_r_addr = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_andi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_lui ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    inst_type == `Inst_jalr ? 0 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? 0 : 
                    inst_type == `Inst_xor ? 0 : 
                    inst_type == `Inst_sltu ? 0 : 
                    inst_type == `Inst_sltiu ? 0 : 
                    inst_type == `Inst_sub ? 0 : 
                    inst_type == `Inst_add ? 0 : 
                    inst_type == `Inst_srl ? 0 : 
                    inst_type == `Inst_srai ? 0 : 
                    inst_type == `Inst_sra ? 0 : 
                    inst_type == `Inst_srli ? 0 : 
                    inst_type == `Inst_slli ? 0 : 
                    inst_type == `Inst_lbu ? add_src1_imm : 
                    inst_type == `Inst_lw ? add_src1_imm : 
                    inst_type == `Inst_sh ? 0 : 
                    inst_type == `Inst_sw ? 0 : 
                    0;


assign mem_r_mask = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_andi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_lui ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    inst_type == `Inst_jalr ? 0 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? 0 : 
                    inst_type == `Inst_xor ? 0 : 
                    inst_type == `Inst_sltu ? 0 : 
                    inst_type == `Inst_sltiu ? 0 : 
                    inst_type == `Inst_sub ? 0 : 
                    inst_type == `Inst_add ? 0 : 
                    inst_type == `Inst_srl ? 0 : 
                    inst_type == `Inst_srai ? 0 : 
                    inst_type == `Inst_sra ? 0 : 
                    inst_type == `Inst_srli ? 0 : 
                    inst_type == `Inst_slli ? 0 : 
                    inst_type == `Inst_lbu ? 8'b00000001 : 
                    inst_type == `Inst_lw ? 8'b00001111 : 
                    inst_type == `Inst_sh ? 0 : 
                    inst_type == `Inst_sw ? 0 : 
                    0;


assign mem_we = inst_type == `Inst_addi ? `Disable : 
                inst_type == `Inst_andi ? `Disable : 
                inst_type == `Inst_auipc ? `Disable : 
                inst_type == `Inst_lui ? `Disable : 
                inst_type == `Inst_jal ? `Disable : 
                inst_type == `Inst_jalr ? `Disable : 
                inst_type == `Inst_beq ? `Disable : 
                inst_type == `Inst_bne ? `Disable : 
                inst_type == `Inst_bgeu ? `Disable : 
                inst_type == `Inst_bltu ? `Disable : 
                inst_type == `Inst_blt ? `Disable : 
                inst_type == `Inst_bge ? `Disable : 
                inst_type == `Inst_or ? `Disable : 
                inst_type == `Inst_xor ? `Disable : 
                inst_type == `Inst_sltu ? `Disable : 
                inst_type == `Inst_sltiu ? `Disable : 
                inst_type == `Inst_sub ? `Disable : 
                inst_type == `Inst_add ? `Disable : 
                inst_type == `Inst_srl ? `Disable : 
                inst_type == `Inst_srai ? `Disable : 
                inst_type == `Inst_sra ? `Disable : 
                inst_type == `Inst_srli ? `Disable : 
                inst_type == `Inst_slli ? `Disable : 
                inst_type == `Inst_lbu ? `Disable : 
                inst_type == `Inst_lw ? `Disable : 
                inst_type == `Inst_sh ? `Enable : 
                inst_type == `Inst_sw ? `Enable : 
                `Disable;


assign mem_w_addr = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_andi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_lui ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    inst_type == `Inst_jalr ? 0 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? 0 : 
                    inst_type == `Inst_xor ? 0 : 
                    inst_type == `Inst_sltu ? 0 : 
                    inst_type == `Inst_sltiu ? 0 : 
                    inst_type == `Inst_sub ? 0 : 
                    inst_type == `Inst_add ? 0 : 
                    inst_type == `Inst_srl ? 0 : 
                    inst_type == `Inst_srai ? 0 : 
                    inst_type == `Inst_sra ? 0 : 
                    inst_type == `Inst_srli ? 0 : 
                    inst_type == `Inst_slli ? 0 : 
                    inst_type == `Inst_lbu ? 0 : 
                    inst_type == `Inst_lw ? 0 : 
                    inst_type == `Inst_sh ? add_src1_imm : 
                    inst_type == `Inst_sw ? add_src1_imm : 
                    0;


assign mem_w_data = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_andi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_lui ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    inst_type == `Inst_jalr ? 0 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? 0 : 
                    inst_type == `Inst_xor ? 0 : 
                    inst_type == `Inst_sltu ? 0 : 
                    inst_type == `Inst_sltiu ? 0 : 
                    inst_type == `Inst_sub ? 0 : 
                    inst_type == `Inst_add ? 0 : 
                    inst_type == `Inst_srl ? 0 : 
                    inst_type == `Inst_srai ? 0 : 
                    inst_type == `Inst_sra ? 0 : 
                    inst_type == `Inst_srli ? 0 : 
                    inst_type == `Inst_slli ? 0 : 
                    inst_type == `Inst_lbu ? 0 : 
                    inst_type == `Inst_lw ? 0 : 
                    inst_type == `Inst_sh ? src2 : 
                    inst_type == `Inst_sw ? src2 : 
                    0;


assign mem_w_mask = inst_type == `Inst_addi ? 0 : 
                    inst_type == `Inst_andi ? 0 : 
                    inst_type == `Inst_auipc ? 0 : 
                    inst_type == `Inst_lui ? 0 : 
                    inst_type == `Inst_jal ? 0 : 
                    inst_type == `Inst_jalr ? 0 : 
                    inst_type == `Inst_beq ? 0 : 
                    inst_type == `Inst_bne ? 0 : 
                    inst_type == `Inst_bgeu ? 0 : 
                    inst_type == `Inst_bltu ? 0 : 
                    inst_type == `Inst_blt ? 0 : 
                    inst_type == `Inst_bge ? 0 : 
                    inst_type == `Inst_or ? 0 : 
                    inst_type == `Inst_xor ? 0 : 
                    inst_type == `Inst_sltu ? 0 : 
                    inst_type == `Inst_sltiu ? 0 : 
                    inst_type == `Inst_sub ? 0 : 
                    inst_type == `Inst_add ? 0 : 
                    inst_type == `Inst_srl ? 0 : 
                    inst_type == `Inst_srai ? 0 : 
                    inst_type == `Inst_sra ? 0 : 
                    inst_type == `Inst_srli ? 0 : 
                    inst_type == `Inst_slli ? 0 : 
                    inst_type == `Inst_lbu ? 0 : 
                    inst_type == `Inst_lw ? 0 : 
                    inst_type == `Inst_sh ? 8'b00000011 : 
                    inst_type == `Inst_sw ? 8'b00001111 : 
                    0;


assign sranum = inst_type == `Inst_addi ? 0 : 
                inst_type == `Inst_andi ? 0 : 
                inst_type == `Inst_auipc ? 0 : 
                inst_type == `Inst_lui ? 0 : 
                inst_type == `Inst_jal ? 0 : 
                inst_type == `Inst_jalr ? 0 : 
                inst_type == `Inst_beq ? 0 : 
                inst_type == `Inst_bne ? 0 : 
                inst_type == `Inst_bgeu ? 0 : 
                inst_type == `Inst_bltu ? 0 : 
                inst_type == `Inst_blt ? 0 : 
                inst_type == `Inst_bge ? 0 : 
                inst_type == `Inst_or ? 0 : 
                inst_type == `Inst_xor ? 0 : 
                inst_type == `Inst_sltu ? 0 : 
                inst_type == `Inst_sltiu ? 0 : 
                inst_type == `Inst_sub ? 0 : 
                inst_type == `Inst_add ? 0 : 
                inst_type == `Inst_srl ? 0 : 
                inst_type == `Inst_srai ? shamt_long : 
                inst_type == `Inst_sra ? src2[4:0] : 
                inst_type == `Inst_srli ? 0 : 
                inst_type == `Inst_slli ? 0 : 
                inst_type == `Inst_lbu ? 0 : 
                inst_type == `Inst_lw ? 0 : 
                inst_type == `Inst_sh ? 0 : 
                inst_type == `Inst_sw ? 0 : 
                0;



endmodule


