`include "defines.v"

module ifu(
        input rst,
        input clk,

        //从PC输入
        input [`InstAddrBus] addr,

        //输出到IDU
        output [`InstDataBus] inst,
        output reg [`InstAddrBus] ifu_pc
);
inst_rom inst_rom1(
        .rst(rst),
        .addr(addr),
        .inst(inst)
);

assign ifu_pc = addr;

endmodule
