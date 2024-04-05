`include "defines.v"

module inst_rom(
        input wire rst, //使能信号
        input wire [`InstAddrBus] addr, //要读取的指令地址

        output reg[`InstDataBus] inst   //读出的指令
);

reg [7:0] insts[0:`InstRomSize-1]/* verilator public */;

wire [`InstAddrBus] actual_addr;
assign  actual_addr = addr - `Init_Addr; //减去0x80000000


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
wire [16:0]index = actual_addr[16:0];  //目前只需要17位地址
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


wire [6:0]opcode = inst[6:0];
assign inst = rst == `RstEnable ? 32'b0 : {insts[index+3],insts[index+2],insts[index+1],insts[index]};

endmodule