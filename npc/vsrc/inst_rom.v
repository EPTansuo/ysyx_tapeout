`include "defines.v"

module inst_rom(
        input wire ce, //使能信号
        input wire [`InstAddrBus] addr, //要读取的指令地址

        output reg[`InstDataBus] inst   //读出的指令
);

reg[`InstDataBus] inst_mem[`InstMemNum-1:0];

assign inst = ce == `RstEnable ? `ZeroWord : inst_mem[addr];


endmodule