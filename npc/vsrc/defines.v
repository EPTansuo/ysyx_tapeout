
`define WriteEnable	1'b1		//写使能
`define WriteDisable	1'b0		//写失能
`define ReadEnable	1'b1		//读使能
`define ReadDisable	1'b0		//读失能
`define AluSelBus 	2:0		//译码阶段的alusel的位宽

`define RstEnable	        1'b1		//复位使能
`define RstDisable	1'b0		//复位失能
`define RegAddrBus	4:0		//GPR 模块的地址线位宽
`define RegBus		63:0		//GPR 模块的数据线位宽
`define RegWidth	        64		//寄存器的位宽
`define RegNum            32              //寄存器的数量

`define InstAddrBus       63:0            //指令地址线位宽
`define InstAddrWidth     64              //指令地址位宽
`define InstDataBus       31:0            //指令数据线位宽
`define InstDataWidth     32              //指令数据位宽

`define InstMemNum	131071		//ROM的实际大小为128kB
`define ZeroWord  	32'h00000000	//32位的0

`define TypeI          3'b000          //I型