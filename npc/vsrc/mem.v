`include "defines.v"
`include "inst_def.v"


module mem(
        input clk,
        input rst,

        //从EXU传入
        input wire we,
        input wire re,
        input wire [`InstAddrBus] w_addr,
        input wire [`InstAddrBus] r_addr,
        input wire [`WordBus] w_data,

        //传输到EXU
        output reg [`WordBus] r_data
);


reg [7:0] mems[0:`MemSize-1]/* verilator public */;

wire [`InstAddrBus] actual_w_addr;
assign  actual_w_addr = w_addr - `Init_Addr; //减去0x80000000

wire [`InstAddrBus] actual_r_addr;
assign  actual_r_addr = r_addr - `Init_Addr; //减去0x80000000

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
wire [16:0]w_index = actual_w_addr[16:0];  //目前只需要17位地址
wire [16:0]r_index = actual_r_addr[16:0];  //目前只需要17位地址
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

//异步读
assign r_data = rst == `RstEnable ? 32'b0 : 
        re == `Disable ? 32'b0 :
        {mems[r_index+3],mems[r_index+2],mems[r_index+1],mems[r_index]};

//同步写
always @(posedge clk) begin
        if(rst == `RstEnable) begin
                // Do nothing
        end
        else  begin
                if(we == `Enable)begin
                        {mems[w_index+3],mems[w_index+2],mems[w_index+1],mems[w_index]} = w_data;
                end
        end
end

endmodule


