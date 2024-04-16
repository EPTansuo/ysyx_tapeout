`include "defines.v"


module cpu(
        input wire rst,
        input wire clk
);

wire [`InstAddrBus]pc;
wire [`InstDataBus]inst;
wire [`InstAddrBus]pc_offset;
wire pc_offset_en;

pc pc1(
        .rst(rst),
        .clk(clk),
        .pc(pc),
        .pc_offset(pc_offset),
        .pc_offset_en(pc_offset_en)
);

wire [`InstAddrBus] ifu_pc;

ifu ifu1(
        .rst(rst),
        .clk(clk),
        .addr(pc),
        .inst(inst),
        .ifu_pc(ifu_pc)
);


wire [`RegDataBus] gpr_rdata_1;
wire [`RegDataBus] gpr_rdata_2;
wire [`RegDataBus] imm;
wire [`RegDataBus] src1;
wire [`RegDataBus] src2;
wire [`RegAddrBus] rd;
wire [7:0] inst_type;
wire [`RegAddrBus] gpr_raddr_1;
wire [`RegAddrBus] gpr_raddr_2;
wire [`InstAddrBus] idu_pc;

idu idu1(
        .rst(rst),
        .clk(clk),
        .inst(inst),
        .rs1(gpr_raddr_1),
        .rs2(gpr_raddr_2),
        .r_data1(gpr_rdata_1),
        .r_data2(gpr_rdata_2),
        .rd(rd),
        .inst_type(inst_type),
        .src1(src1),
        .src2(src2),
        .imm(imm),
        .ifu_pc(ifu_pc),
        .idu_pc(idu_pc)
);

wire gpr_we;
wire [`RegAddrBus] gpr_waddr;
wire [`RegDataBus] gpr_wdata;

gpr gpr1(
        .clk(clk),
        .rst(rst),
        .we(gpr_we),
        .waddr(gpr_waddr),
        .wdata(gpr_wdata),
        .raddr1(gpr_raddr_1),
        .rdata1(gpr_rdata_1),
        .raddr2(gpr_raddr_2),
        .rdata2(gpr_rdata_2)
);

wire exu_mem_we;
wire exu_mem_re;
wire [`InstAddrBus] exu_mem_w_addr;
wire [`InstAddrBus] exu_mem_r_addr;
wire [`WordBus] exu_mem_w_data;
wire [`WordBus] exu_mem_r_data;

exu exu1(
        .clk(clk),
        .rst(rst),
        .src1(src1),
        .src2(src2),
        .imm(imm),
        .inst_type(inst_type),
        .rd(rd),
        .w_data(gpr_wdata),
        .w_addr(gpr_waddr),
        .we(gpr_we),
        .idu_pc(idu_pc),
        .pc_offset_en(pc_offset_en),
        .pc_offset(pc_offset),
        .mem_we(exu_mem_we),
        .mem_re(exu_mem_re),
        .mem_w_addr(exu_mem_w_addr),
        .mem_r_addr(exu_mem_w_addr),
        .mem_w_data(exu_mem_w_data),
        .mem_r_data(exu_mem_r_data)
);

mem mem1(
        .clk(clk),
        .rst(rst),
        .we(exu_mem_we),
        .w_addr(exu_mem_w_addr),
        .w_data(exu_mem_w_addr),
        .re(exu_mem_re),
        .r_addr(exu_mem_r_addr),
        .r_data(exu_mem_r_data)
);




endmodule