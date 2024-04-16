`include "defines.v"

module pc(
        input wire clk,
        input wire rst,

        //从EXU传入
        input wire pc_offset_en,
        input wire [`InstAddrBus] pc_offset, //下一条指令地址

        //传输到IFU
        output reg[`InstAddrBus] pc/* verilator public */ //下一条指令地址
);

//reg[`InstAddrBus] pc; //当前的指令地址

always @(posedge clk) begin
        if(rst == `RstEnable)begin
                //pc <= `Init_Addr;
                pc <= `Init_Addr -4;
        end
        else begin
                //pc <= npc;
                if(pc_offset_en == `Enable)begin
                        pc <= pc + pc_offset;
                end
                else begin 
                        pc <= pc + 4;
                end
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