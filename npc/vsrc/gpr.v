`include "defines.v"

module gpr(
        input clk,
        input rst,

        //写端口
	input	we,		//使能信号
	input [`RegAddrBus]	waddr,	//要写入的寄存器地址
	input [`RegDataBus]	wdata,	//要写入的数据     

	//读端口1
	//input wire	re1,		//使能信号
	input [`RegAddrBus]	raddr1, //要读取的寄存器地址
	output reg[`RegDataBus]	rdata1, //要读取的数据  
        
        //读端口2
	//input wire	re2,		//使能信号
	input [`RegAddrBus]	raddr2, //要读取的寄存器地址
	output reg[`RegDataBus]	rdata2  //要读取的数据     
);


reg[`RegDataBus] regs[`RegNum-1:0]/* verilator public */;   //对于rv64，有32个通用寄存器，位宽为64位


//同步写
always @(posedge clk) begin
	if(rst == `RstEnable)begin
		regs[0] <= `ZeroWord;
	end
	else begin
		if(we == `WriteEnable )begin
			regs[waddr] <= waddr == 0 ? `ZeroWord : wdata; // $0寄存器始终为0
		end
		else begin
			regs[0] <= `ZeroWord; // Ensure regs[0] is always assigned
		end
	end
end


//异步读
assign rdata1 = raddr1 == 0 ? `ZeroWord : regs[raddr1];
assign rdata2 = raddr2 == 0 ? `ZeroWord : regs[raddr2];

endmodule