//`define CONFIG_RV64
`define CONFIG_RV32

`define WriteEnable	1'b1		//写使能
`define WriteDisable	1'b0		//写失能
`define ReadEnable	1'b1		//读使能
`define ReadDisable	1'b0		//读失能
`define AluSelBus 	2:0		//译码阶段的alusel的位宽

`define RstEnable	1'b1		//复位使能
`define RstDisable	1'b0		//复位失能
`define RegAddrBus	4:0		//GPR 模块的地址线位宽
`define RegNum          32              //寄存器的数量

`define InstDataBus     31:0            //指令数据线位宽
`define InstDataWidth   32              //指令数据位宽

`define InstRomSize	131072		//ROM的实际大小为128kB



`define OP_I_TYPE	7'b0010011	//I型指令的操作码
`define OP_S_TYPE	7'b0100011	//S型指令的操作码
`define OP_J_TYPE	7'b1101111	//J型指令的操作码
`define OP_B_TYPE	7'b1100011	//B型指令的操作码
`define OP_R_TYPE	7'b0110011	//R型指令的操作码
`define OP_R_TYPE_W     7'b0111011	//R型指令的操作码


`define Inst_addi       8'd0            //代表addi指令



`define EBREAK          32'h00100073	//ebreak指令


`ifdef CONFIG_RV64         //对于RV64的配置

`define RegDataBus	63:0		//GPR 模块的数据线位宽
`define RegWidth        64		//寄存器的位宽
`define InstAddrBus     63:0            //指令地址线位宽
`define InstAddrWidth   64              //指令地址位宽
`define Init_Addr       64'h80000000	//初始化的地址
`define ZeroWord  	64'h0    	//64位的0
`define WordBus         63:0            //数据总线
`define WordWidth       64              //数据总线位宽

`else                      //对于RV32的配置

`define RegDataBus	31:0		//GPR 模块的数据线位宽
`define RegWidth        31		//寄存器的位宽
`define InstAddrBus     31:0            //指令地址线位宽
`define InstAddrWidth   32              //指令地址位宽
`define Init_Addr       32'h80000000	//初始化的地址
`define ZeroWord  	32'h0    	//32位的0
`define WordBus         31:0            //数据总线位宽
`define WordWidth       32              //数据总线位宽
`endif

