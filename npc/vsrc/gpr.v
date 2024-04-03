`include "defines.v"

module gpr(
        input wire clk,
        input wire rst,

        //写端口
	input wire	we,		//使能信号
	input wire[`RegAddrBus]	waddr,	//要写入的寄存器地址
	input wire[`RegBus]	wdata,	//要写入的数据     

	//读端口1
	input wire	re1,		//使能信号
	input wire[`RegAddrBus]	raddr1, //要读取的寄存器地址
	output reg[`RegBus]	rdata1, //要读取的数据  
        
        //读端口2
	input wire	re2,		//使能信号
	input wire[`RegAddrBus]	raddr2, //要读取的寄存器地址
	output reg[`RegBus]	rdata2  //要读取的数据     
);

reg[`RegBus] regs[`RegNum-1:0];

endmodule