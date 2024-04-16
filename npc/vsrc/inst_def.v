
`define OP_I_TYPE	7'b0010011	//I型指令的操作码
`define OP_I_TYPE_W	7'b0011011	//I型xxxw指令的操作码
`define OP_I_TYPE_L     7'b0000011	//I型load指令的操作码
`define OP_I_TYPE_jarl  7'b1100111	//I型jalr指令的操作码

`define OP_S_TYPE	7'b0100011	//S型指令的操作码
`define OP_J_TYPE	7'b1101111	//J型指令的操作码
`define OP_B_TYPE	7'b1100011	//B型指令的操作码
`define OP_R_TYPE	7'b0110011	//R型指令的操作码
`define OP_R_TYPE_W     7'b0111011	//R型xxxw指令的操作码
`define OP_U_TYPE_aupic	7'b0010111	//U型指令aupic操作码
`define OP_U_TYPE_lui   7'b0110111	//U型指令lui操作码

`define Inst_inv        8'd0            //无效指令 
`define Inst_addi       8'd1            //代表addi指令
`define Inst_auipc      8'd2            //auipc
`define Inst_lui        8'd3            //lui
`define Inst_jal        8'd4            //jal
`define Inst_jalr       8'd5            //jalr
`define Inst_sw         8'd6            //sw
`define Inst_lw         8'd7            //lw
`define Inst_add        8'd8            //add
`define Inst_beq        8'd9            //beq


`define EBREAK          32'h00100073	//ebreak指令

`define Funct3_sw       3'b010          //sw指令的funct3
`define Funct3_addi     3'b000          //addi指令的funct3
`define Funct3_jalr     3'b000          //jalr指令的funct3
