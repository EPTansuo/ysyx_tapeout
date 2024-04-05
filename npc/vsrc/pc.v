`include "defines.v"

module pc(
        input wire clk,
        input wire rst,

        
        output reg[`InstAddrBus] npc //下一条指令地址
);

reg[`InstAddrBus] pc; //当前的指令地址

always @(posedge clk) begin
        if(rst == `RstEnable)begin
                //pc <= `Init_Addr;
                npc <= `Init_Addr;
        end
        else begin
                //pc <= npc;
                npc <= npc + 1;
        end
end


// Reg #(`InstAddrWidth, 0) pc_reg (
//         .clk(clk),
//         .rst(rst),
//         .din(pc),
//         .dout(pc + 1),
//         .wen(ce)
// );

endmodule