//`define CONFIG_RV64
`define CONFIG_RV32

`define WriteEnable	1'b1		//写使能
`define WriteDisable	1'b0		//写失能
`define ReadEnable	1'b1		//读使能
`define ReadDisable	1'b0		//读失能
//`define AluSelBus 	2:0		//译码阶段的alusel的位宽
`define Enable          1'b1            //使能
`define Disable         1'b0            //失能

`define RstEnable	1'b1		//复位使能
`define RstDisable	1'b0		//复位失能
`define RegAddrBus	4:0		//GPR 模块的地址线位宽
`define RegNum          32              //寄存器的数量

`define InstDataBus     31:0            //指令数据线位宽
`define InstDataWidth   32              //指令数据位宽

`define InstRomSize	131072		//ROM的实际大小为128kB
`define MemSize 	131072		//MEM的实际大小为128kB


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
`endif // CONFIG_RV64

`define ONE_BYTE         2'b00          //代表内存读写1个字节
`define TWO_BYTES        2'b01          //2字节
`define FOUR_BYTES       2'b10          //4字节
`define EIGHT_BYTES      2'b11          //8字节         
