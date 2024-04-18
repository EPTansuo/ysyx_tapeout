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
        output reg [`RegDataBus] gpr_w_data,
        output reg [`RegAddrBus] gpr_w_addr,
        output reg gpr_we,

        //传输到PC
        output reg [`InstAddrBus] pc_offset,
        output reg pc_offset_en,

        //传输到MEM
        output reg mem_we,
        output reg mem_re,
        output reg [`InstAddrBus] mem_w_addr,
        output reg [`InstAddrBus] mem_r_addr,
        output reg [`WordBus] mem_w_data,
        output reg [1:0] mem_w_bytes,
        output reg [1:0] mem_r_bytes,

        //从MEM传入
        input wire [`WordBus] mem_r_data
);

`ifndef STA

import "DPI-C" function void npc_ebreak();
import "DPI-C" function void inst_invalid();

// always @(*) begin
//         if(inst == `EBREAK)begin
//                 npc_ebreak();
//         end
// end

`endif




wire [`WordBus] add_src1_imm;

assign add_src1_imm = src1+imm;


always @(*) begin
        case (inst_type)
                `Inst_addi: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = add_src1_imm;
                        gpr_w_addr = rd;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_auipc: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = add_src1_imm;//src1被设为pc，则aupic可重复利用addi的加法器
                        gpr_w_addr = rd;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;

                end
                `Inst_lui: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = imm;
                        gpr_w_addr = rd;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_jal: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = idu_pc + 4;
                        gpr_w_addr = rd;
                        pc_offset_en = `Enable;
                        pc_offset = imm;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_jalr: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = idu_pc + 4;
                        gpr_w_addr = rd;
                        pc_offset_en = `Enable;
                        pc_offset = {add_src1_imm[`WordWidth-1:1], 1'b0} - idu_pc;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_sw: begin
                        mem_we = `Enable;
                        mem_re = `Disable;
                        mem_w_addr = add_src1_imm;
                        mem_r_addr = src2;
                        gpr_we = `Disable;
                        gpr_w_data = 0;
                        gpr_w_addr = 0;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_lw: begin
                        mem_we = `Disable;
                        mem_re = `Enable;
                        mem_w_addr = 0;
                        mem_r_addr = add_src1_imm;
                        gpr_we = `Disable;
                        gpr_w_data = 0;
                        gpr_w_addr = 0;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_add: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Enable;
                        gpr_w_data = src1 + src2;
                        gpr_w_addr = rd;
                        pc_offset_en = `Disable;
                        pc_offset = 4;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                end
                `Inst_beq: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Disable;
                        gpr_w_data = 0;
                        gpr_w_addr = 0;
                        pc_offset_en = src1 == src2? `Enable : `Disable;;
                        pc_offset = imm;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                        
                end
                `Inst_ebreak: begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Disable;
                        gpr_w_data = 0;
                        gpr_w_addr = 0;
                        pc_offset_en = `Disable;
                        pc_offset = 0;
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                        `ifndef STA
                        npc_ebreak();
                        `endif
                end
                default:begin
                        mem_we = `Disable;
                        mem_re = `Disable;
                        mem_w_addr = 0;
                        mem_r_addr = 0;
                        gpr_we = `Disable;
                        gpr_w_data = 0;
                        gpr_w_addr = 0;
                        pc_offset_en = `Disable;
                        pc_offset = 0;
                        `ifndef STA
                        mem_w_bytes = `FOUR_BYTES;
                        mem_r_bytes = `FOUR_BYTES;
                        if(idu_pc >= `Init_Addr) begin
                                inst_invalid();
                        end
                        `endif

                end
        endcase
end


endmodule

