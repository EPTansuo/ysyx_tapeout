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
wire [`InstDataBus] idu_inst;
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
        .idu_pc(idu_pc),
        .idu_inst(idu_inst)
);

wire gpr_we;
wire [`RegAddrBus] gpr_waddr;
wire [`RegDataBus] gpr_wdata;
wire [`RegDataBus] gpr_a7;
gpr gpr1(
        .clk(clk),
        .rst(rst),
        .we(gpr_we),
        .waddr(gpr_waddr),
        .wdata(gpr_wdata),
        .raddr1(gpr_raddr_1),
        .rdata1(gpr_rdata_1),
        .raddr2(gpr_raddr_2),
        .rdata2(gpr_rdata_2),
        .a7(gpr_a7)
);

wire exu_mem_we;
wire exu_mem_re;
wire [`InstAddrBus] exu_mem_w_addr;
wire [`InstAddrBus] exu_mem_r_addr;
wire [`WordBus] exu_mem_w_data;
wire [`WordBus] exu_mem_r_data;
wire [7:0] exu_mem_w_mask;
wire [7:0] exu_mem_r_mask;
wire [`RegDataBus] exu_csr_rdata;
wire [`InstAddrBus] exu_csr_pc;
wire [7:0] exu_csr_inst_type;
wire [11:0] exu_csr_raddr;
wire [`RegDataBus] exu_csr_wdata;
wire [11:0] exu_csr_waddr;
wire exu_csr_we;

exu exu1(
        .clk(clk),
        .rst(rst),
        .src1(src1),
        .src2(src2),
        .imm(imm),
        .inst_type(inst_type),
        .rd(rd),
        .gpr_w_data(gpr_wdata),
        .gpr_w_addr(gpr_waddr),
        .gpr_we(gpr_we),
        .idu_pc(idu_pc),
        .pc_offset_en(pc_offset_en),
        .pc_offset(pc_offset),
        .mem_we(exu_mem_we),
        .mem_re(exu_mem_re),
        .mem_w_addr(exu_mem_w_addr),
        .mem_r_addr(exu_mem_r_addr),
        .mem_w_data(exu_mem_w_data),
        .mem_r_data(exu_mem_r_data),
        .mem_w_mask(exu_mem_w_mask),
        .mem_r_mask(exu_mem_r_mask),
        .idu_inst(idu_inst),

        //from pc 
        .exu_pc(idu_pc),
        
        //from csr
        .csr_rdata(exu_csr_rdata),

        //to csr
        .exu_csr_pc(exu_csr_pc),
        .exu_csr_inst_type(exu_csr_inst_type),
        .csr_raddr(exu_csr_raddr),
        .csr_wdata(exu_csr_wdata),
        .csr_waddr(exu_csr_waddr),
        .csr_we(exu_csr_we)
);


csr csr1(
        .clk(clk),
        .rst(rst),
        .we(exu_csr_we),
        .waddr(exu_csr_waddr),
        .wdata(exu_csr_wdata),
        .raddr(exu_csr_raddr),
        .rdata(exu_csr_rdata),
        .inst_type(exu_csr_inst_type),
        .exu_pc(exu_csr_pc),
        .gpr_a7(gpr_a7)
);

mem mem1(
        .clk(clk),
        .rst(rst),
        .we(exu_mem_we),
        .w_addr(exu_mem_w_addr),
        .w_data(exu_mem_w_data),
        .re(exu_mem_re),
        .r_addr(exu_mem_r_addr),
        .r_data(exu_mem_r_data),
        .r_mask(exu_mem_r_mask),
        .w_mask(exu_mem_w_mask)
);




endmodule
