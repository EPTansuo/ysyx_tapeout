`include "defines.v"

module ifu(
        input rst,
        input clk,

        input [`InstAddrBus] addr,
        output [`InstDataBus] inst
);
inst_rom inst_rom1(
        .rst(rst),
        .addr(addr),
        .inst(inst)
);

endmodule
