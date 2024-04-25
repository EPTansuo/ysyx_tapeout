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



wire funct3_100;
wire funct3_001;
wire funct3_101;
wire funct3_010;
wire funct3_111;
wire funct3_000;
wire funct3_011;
wire funct3_110;


wire funct7_0000000;
wire funct7_0100000;
wire funct7_6_010000;
wire funct7_6_000000;
wire funct7_0000001;


wire opcode_0010111;
wire opcode_0110111;
wire opcode_0000011;
wire opcode_0010011;
wire opcode_0011011;
wire opcode_1100111;
wire opcode_0100011;
wire opcode_1101111;
wire opcode_1100011;
wire opcode_0111011;
wire opcode_0110011;


wire inst_pat_auipc;
wire inst_pat_lui;
wire inst_pat_lbu;
wire inst_pat_lh;
wire inst_pat_lhu;
wire inst_pat_lw;
wire inst_pat_ld;
wire inst_pat_andi;
wire inst_pat_addi;
wire inst_pat_sltiu;
wire inst_pat_xori;
wire inst_pat_srai;
wire inst_pat_srli;
wire inst_pat_slli;
wire inst_pat_addiw;
wire inst_pat_srliw;
wire inst_pat_sraiw;
wire inst_pat_slliw;
wire inst_pat_jalr;
wire inst_pat_sd;
wire inst_pat_sw;
wire inst_pat_sh;
wire inst_pat_sb;
wire inst_pat_jal;
wire inst_pat_bne;
wire inst_pat_beq;
wire inst_pat_blt;
wire inst_pat_bge;
wire inst_pat_bltu;
wire inst_pat_bgeu;
wire inst_pat_addw;
wire inst_pat_mulw;
wire inst_pat_subw;
wire inst_pat_sllw;
wire inst_pat_sraw;
wire inst_pat_srlw;
wire inst_pat_divuw;
wire inst_pat_remw;
wire inst_pat_remuv;
wire inst_pat_divw;
wire inst_pat_sub;
wire inst_pat_add;
wire inst_pat_mul;
wire inst_pat_and;
wire inst_pat_remu;
wire inst_pat_or;
wire inst_pat_rem;
wire inst_pat_xor;
wire inst_pat_div;
wire inst_pat_sltu;
wire inst_pat_sra;
wire inst_pat_srl;
wire inst_pat_divu;
wire inst_pat_sll;


assign funct3_100 = (funct3 == 3'b100);
assign funct3_001 = (funct3 == 3'b001);
assign funct3_101 = (funct3 == 3'b101);
assign funct3_010 = (funct3 == 3'b010);
assign funct3_111 = (funct3 == 3'b111);
assign funct3_000 = (funct3 == 3'b000);
assign funct3_011 = (funct3 == 3'b011);
assign funct3_110 = (funct3 == 3'b110);


assign funct7_0000000 = (funct7 == 7'b0000000);
assign funct7_0100000 = (funct7 == 7'b0100000);
assign funct7_6_010000 = (funct7[6:1] == 6'b010000);
assign funct7_6_000000 = (funct7[6:1] == 6'b000000);
assign funct7_0000001 = (funct7 == 7'b0000001);


assign opcode_0010111 = (opcode == 7'b0010111);
assign opcode_0110111 = (opcode == 7'b0110111);
assign opcode_0000011 = (opcode == 7'b0000011);
assign opcode_0010011 = (opcode == 7'b0010011);
assign opcode_0011011 = (opcode == 7'b0011011);
assign opcode_1100111 = (opcode == 7'b1100111);
assign opcode_0100011 = (opcode == 7'b0100011);
assign opcode_1101111 = (opcode == 7'b1101111);
assign opcode_1100011 = (opcode == 7'b1100011);
assign opcode_0111011 = (opcode == 7'b0111011);
assign opcode_0110011 = (opcode == 7'b0110011);


assign inst_pat_auipc = opcode_0010111;

assign inst_pat_lui = opcode_0110111;

assign inst_pat_lbu = opcode_0000011 &
                      funct3_100;

assign inst_pat_lh = opcode_0000011 &
                     funct3_001;

assign inst_pat_lhu = opcode_0000011 &
                      funct3_101;

assign inst_pat_lw = opcode_0000011 &
                     funct3_010;

assign inst_pat_ld = opcode_0000011 &
                     funct3_011;

assign inst_pat_andi = opcode_0010011 &
                       funct3_111;

assign inst_pat_addi = opcode_0010011 &
                       funct3_000;

assign inst_pat_sltiu = opcode_0010011 &
                        funct3_011;

assign inst_pat_xori = opcode_0010011 &
                       funct3_100;

assign inst_pat_srai = opcode_0010011 &
                       funct3_101 &
                       funct7_6_010000;

assign inst_pat_srli = opcode_0010011 &
                       funct3_101 &
                       funct7_6_000000;

assign inst_pat_slli = opcode_0010011 &
                       funct3_001;

assign inst_pat_addiw = opcode_0011011 &
                        funct3_000;

assign inst_pat_srliw = opcode_0011011 &
                        funct3_101 &
                        funct7_0000000;

assign inst_pat_sraiw = opcode_0011011 &
                        funct3_101 &
                        funct7_0100000;

assign inst_pat_slliw = opcode_0011011 &
                        funct3_001;

assign inst_pat_jalr = opcode_1100111;

assign inst_pat_sd = opcode_0100011 &
                     funct3_011;

assign inst_pat_sw = opcode_0100011 &
                     funct3_010;

assign inst_pat_sh = opcode_0100011 &
                     funct3_001;

assign inst_pat_sb = opcode_0100011 &
                     funct3_000;

assign inst_pat_jal = opcode_1101111;

assign inst_pat_bne = opcode_1100011 &
                      funct3_001;

assign inst_pat_beq = opcode_1100011 &
                      funct3_000;

assign inst_pat_blt = opcode_1100011 &
                      funct3_100;

assign inst_pat_bge = opcode_1100011 &
                      funct3_101;

assign inst_pat_bltu = opcode_1100011 &
                       funct3_110;

assign inst_pat_bgeu = opcode_1100011 &
                       funct3_111;

assign inst_pat_addw = opcode_0111011 &
                       funct3_000 &
                       funct7_0000000;

assign inst_pat_mulw = opcode_0111011 &
                       funct3_000 &
                       funct7_0000001;

assign inst_pat_subw = opcode_0111011 &
                       funct3_000 &
                       funct7_0100000;

assign inst_pat_sllw = opcode_0111011 &
                       funct3_001;

assign inst_pat_sraw = opcode_0111011 &
                       funct3_101 &
                       funct7_0100000;

assign inst_pat_srlw = opcode_0111011 &
                       funct3_101 &
                       funct7_0000000;

assign inst_pat_divuw = opcode_0111011 &
                        funct3_101 &
                        funct7_0000001;

assign inst_pat_remw = opcode_0111011 &
                       funct3_110;

assign inst_pat_remuv = opcode_0111011 &
                        funct3_111;

assign inst_pat_divw = opcode_0111011 &
                       funct3_100;

assign inst_pat_sub = opcode_0110011 &
                      funct3_000 &
                      funct7_0100000;

assign inst_pat_add = opcode_0110011 &
                      funct3_000 &
                      funct7_0000000;

assign inst_pat_mul = opcode_0110011 &
                      funct3_000 &
                      funct7_0000001;

assign inst_pat_and = opcode_0110011 &
                      funct3_111 &
                      funct7_0000000;

assign inst_pat_remu = opcode_0110011 &
                       funct3_111 &
                       funct7_0000001;

assign inst_pat_or = opcode_0110011 &
                     funct3_110 &
                     funct7_0000000;

assign inst_pat_rem = opcode_0110011 &
                      funct3_110 &
                      funct7_0000001;

assign inst_pat_xor = opcode_0110011 &
                      funct3_100 &
                      funct7_0000000;

assign inst_pat_div = opcode_0110011 &
                      funct3_100 &
                      funct7_0000001;

assign inst_pat_sltu = opcode_0110011 &
                       funct3_011;

assign inst_pat_sra = opcode_0110011 &
                      funct3_101 &
                      funct7_0100000;

assign inst_pat_srl = opcode_0110011 &
                      funct3_101 &
                      funct7_0000000;

assign inst_pat_divu = opcode_0110011 &
                       funct3_101 &
                       funct7_0000001;

assign inst_pat_sll = opcode_0110011 &
                      funct3_001;



assign inst_type = inst == `EBREAK ? `Inst_ebreak : 
                   inst_pat_auipc ? `Inst_auipc : 
                   inst_pat_lui ? `Inst_lui : 
                   inst_pat_lbu ? `Inst_lbu : 
                   inst_pat_lh ? `Inst_lh : 
                   inst_pat_lhu ? `Inst_lhu : 
                   inst_pat_lw ? `Inst_lw : 
                   inst_pat_andi ? `Inst_andi : 
                   inst_pat_addi ? `Inst_addi : 
                   inst_pat_addiw ? `Inst_addiw : 
                   inst_pat_sltiu ? `Inst_sltiu : 
                   inst_pat_jalr ? `Inst_jalr : 
                   inst_pat_ld ? `Inst_ld : 
                   inst_pat_xori ? `Inst_xori : 
                   inst_pat_sd ? `Inst_sd : 
                   inst_pat_sw ? `Inst_sw : 
                   inst_pat_sh ? `Inst_sh : 
                   inst_pat_sb ? `Inst_sb : 
                   inst_pat_jal ? `Inst_jal : 
                   inst_pat_bne ? `Inst_bne : 
                   inst_pat_beq ? `Inst_beq : 
                   inst_pat_blt ? `Inst_blt : 
                   inst_pat_bge ? `Inst_bge : 
                   inst_pat_bltu ? `Inst_bltu : 
                   inst_pat_bgeu ? `Inst_bgeu : 
                   inst_pat_addw ? `Inst_addw : 
                   inst_pat_sub ? `Inst_sub : 
                   inst_pat_add ? `Inst_add : 
                   inst_pat_and ? `Inst_and : 
                   inst_pat_or ? `Inst_or : 
                   inst_pat_xor ? `Inst_xor : 
                   inst_pat_sllw ? `Inst_sllw : 
                   inst_pat_sltu ? `Inst_sltu : 
                   inst_pat_srliw ? `Inst_srliw : 
                   inst_pat_slliw ? `Inst_slliw : 
                   inst_pat_sra ? `Inst_sra : 
                   inst_pat_srl ? `Inst_srl : 
                   inst_pat_sraiw ? `Inst_sraiw : 
                   inst_pat_sraw ? `Inst_sraw : 
                   inst_pat_srlw ? `Inst_srlw : 
                   inst_pat_sll ? `Inst_sll : 
                   inst_pat_srai ? `Inst_srai : 
                   inst_pat_slli ? `Inst_slli : 
                   inst_pat_mul ? `Inst_mul : 
                   inst_pat_mulw ? `Inst_mulw : 
                   inst_pat_rem ? `Inst_rem : 
                   inst_pat_remu ? `Inst_remu : 
                   inst_pat_remw ? `Inst_remw : 
                   inst_pat_remuv ? `Inst_remuv : 
                   inst_pat_subw ? `Inst_subw : 
                   inst_pat_srli ? `Inst_srli : 
                   inst_pat_div ? `Inst_div : 
                   inst_pat_divu ? `Inst_divu : 
                   inst_pat_divw ? `Inst_divw : 
                   inst_pat_divuw ? `Inst_divuw : 
                   `Inst_inv;


assign imm = (opcode == 7'b0010111) ? immU : 
             (opcode == 7'b0110111) ? immU : 
             (opcode == 7'b0000011) ? immI : 
             (opcode == 7'b0010011) ? immI : 
             (opcode == 7'b0011011) ? immI : 
             (opcode == 7'b1100111) ? immI : 
             (opcode == 7'b0100011) ? immS : 
             (opcode == 7'b1101111) ? immJ : 
             (opcode == 7'b1100011) ? immB : 
             0;
 

endmodule

