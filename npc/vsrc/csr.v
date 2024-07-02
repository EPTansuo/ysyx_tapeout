`include "defines.v"

module csr(
        input clk,
        input rst,

        //写端口
	input	we,		//使能信号
	input [`RegAddrBus]	waddr,	//要写入的寄存器地址
	input [`RegDataBus]	wdata,	//要写入的数据     

	//读端口1
	//input wire	re1,		//使能信号
	input [`RegAddrBus]	raddr, //要读取的寄存器地址
	output reg[`RegDataBus]	rdata //要读取的数据  
        
    
);


localparam MEPC_INDEX = 0,
           MCAUSE_INDEX = 1,
           MSTATUS_INDEX = 2,
           MTVEC_INDEX = 3;

localparam MEPC_ADDR= 12'h341,
           MCAUSE_ADDR = 12'h342,
           MSTATUS_ADDR = 12'h300,
           MTVEC_ADDR = 12'h305;



reg[`RegDataBus] csrs[4-1:0]/* verilator public */;   //mepc, mcause, mstatus, mtvec

function [1:0]addr2index(input [11:0] addr);
        case(addr)
                MEPC_ADDR: addr2index = MEPC_INDEX;
                MCAUSE_ADDR: addr2index = MCAUSE_INDEX;
                MSTATUS_ADDR: addr2index = MSTATUS_INDEX;
                MTVEC_ADDR: addr2index = MTVEC_INDEX;
                default: addr2index = 2'b11;
        endcase
endfunction

wire [1:0]rindex = addr2index(raddr);
wire [1:0]windex = addr2index(waddr);

//同步写
always @(posedge clk) begin
	if(rst == `RstEnable)begin
                `ifdef CONFIG_RV32
                        csrs[MSTATUS_INDEX] <=32'h1800;
                        csrs[MTVEC_INDEX] <= 32'h100;
                `else 
                        csrs[MSTATUS_INDEX] <= 64'ha00001800
                        csrs[MTVEC_INDEX] <= 64'h80000000;
                `endif 
                csrs[MEPC_INDEX] <= 0;
	end
	else begin
		if(we == `WriteEnable )begin
			csrs[windex] <=  wdata; // $0寄存器始终为0
		end
	end
end


//异步读
assign rdata = csrs[rindex];

endmodule