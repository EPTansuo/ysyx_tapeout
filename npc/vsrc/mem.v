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
        input wire [1:0] w_bytes,   //0: 1Byte, 1: 2Bytes, 2: 4Bytes, 3: 8Bytes
        input wire [1:0] r_bytes,
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
always @( * ) begin
        if(rst == `RstEnable) begin
                // Do nothing
                r_data = 0;
        end
        else  begin
                if(re == `Enable)begin
                        case(r_bytes)
                                `ONE_BYTE: begin //一个字节

                                        r_data = {{(`WordWidth-8){1'b0}}, mems[r_index]};
                                end
                                `TWO_BYTES: begin //两个字节
                                        r_data = { {(`WordWidth-16){1'b0}}, mems[r_index+1], mems[r_index]};
                                end
                                `FOUR_BYTES: begin //四个字节
                                        r_data = {mems[r_index+3],mems[r_index+2],mems[r_index+1],mems[r_index]};
                                end
                                default: begin   //暂时不支持8个字节,RV64才有相关的指令

                                end
                        endcase
                        
                end
        end
end


//同步写
always @(posedge clk) begin
        if(rst == `RstEnable) begin
                // Do nothing
        end
        else  begin
                if(we == `Enable)begin
                        case(w_bytes)
                                `ONE_BYTE: begin //一个字节
                                        mems[w_index] <= w_data[7:0];
                                end
                                `TWO_BYTES: begin //两个字节
                                        {mems[w_index+1], mems[w_index]} <= w_data[15:0];
                                end
                                `FOUR_BYTES: begin //四个字节
                                        {mems[w_index+3],mems[w_index+2],mems[w_index+1],mems[w_index]} <= w_data[31:0]; //[31:0]是为了后续RV64提前准备的
                                end
                                default: begin   //暂时不支持8个字节,RV64才有相关的指令
                                        
                                end
                        endcase
                        
                end
        end
end

endmodule


