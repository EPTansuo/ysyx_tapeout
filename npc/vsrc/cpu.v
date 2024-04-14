`include "defines.v"


module cpu(
        input wire rst,
        input wire clk
);

wire [`InstAddrBus]pc;
wire [`InstDataBus]inst;

pc pc1(
        .rst(rst),
        .clk(clk),
        .pc(pc)
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
        .idu_pc(idu_pc)
);


endmodule