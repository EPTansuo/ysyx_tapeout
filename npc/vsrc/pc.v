`include "defines.v"

module pc(
        input wire clk,
        input wire rst,

        output reg[`InstAddrBus] pc, //当前的指令地址
        output reg[`InstAddrBus] npc, //下一条指令地址
        output reg ce  //使能信号
);

initial begin 
        
end



Reg #(`InstAddrWidth, 0) pc_reg (
        .clk(clk),
        .rst(rst),
        .din(pc),
        .dout(pc + 1),
        .wen(ce)
);

endmodule