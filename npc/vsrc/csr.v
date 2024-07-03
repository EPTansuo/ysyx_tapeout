`include "defines.v"

module csr(
        input clk,
        input rst,

        //写端口
	input	we,		//使能信号
	input [11:0]	waddr,	//要写入的寄存器,实际上是编号
	input [`RegDataBus]	wdata,	//要写入的数据     

	//读端口1
	//input wire	re1,		//使能信号
	input [11:0]	raddr, //要读取的寄存器地址，实际上是编号
	output reg[`RegDataBus]	rdata, //要读取的数据  
        
        //from exu
        input [7:0]inst_type,
        input wire [`InstAddrBus] exu_pc,

        //from grp
        input [`RegDataBus] gpr_a7_a5 //a7 或 a5
);


localparam MEPC_INDEX = 0,
           MCAUSE_INDEX = 1,
           MSTATUS_INDEX = 2,
           MTVEC_INDEX = 3;




reg[`RegDataBus] csrs[4-1:0]/* verilator public */;   //mepc, mcause, mstatus, mtvec

function [1:0]addr2index(input [11:0] addr);
        case(addr)
                `MEPC_NO: addr2index = MEPC_INDEX;
                `MCAUSE_NO: addr2index = MCAUSE_INDEX;
                `MSTATUS_NO: addr2index = MSTATUS_INDEX;
                `MTVEC_NO: addr2index = MTVEC_INDEX;
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
			csrs[windex] <=  wdata; 
                end
                if (inst_type == `Inst_ecall) begin
                        csrs[MSTATUS_INDEX][7] <= csrs[MSTATUS_INDEX][3];
                        csrs[MSTATUS_INDEX][3] <= 0;
                        csrs[MSTATUS_INDEX][12:11] <= 2'b11;
                        csrs[MEPC_INDEX] <= exu_pc;
                        csrs[MCAUSE_INDEX] = gpr_a7_a5;
                       // dnpc = mtvec; //在exu中设置
                end
                else if(inst_type ==  `Inst_mret) begin
                        // dnpc = mepc; //在exu中设置
                        csrs[MSTATUS_INDEX][3] <= csrs[MSTATUS_INDEX][7];
                        csrs[MSTATUS_INDEX][7] <= 1;
                        csrs[MSTATUS_INDEX][12:11] <= 2'b00;
                end
	end
end


//异步读
assign rdata = csrs[rindex];

endmodule