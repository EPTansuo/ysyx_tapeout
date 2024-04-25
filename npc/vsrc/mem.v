`include "defines.v"
`include "inst_def.v"


module mem(
        input clk,
        input rst,

        //from cpu.exu
        input wire we,
        input wire re,
        input wire [`InstAddrBus] w_addr,
        input wire [`InstAddrBus] r_addr,
        input wire [`WordBus] w_data,

        input wire [7:0] w_mask,
        input wire [7:0] r_mask,
        
        //transfer to cpu.exu
        output reg [`WordBus] r_data
);


import "DPI-C" function int pmem_read(input int raddr);
import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);



always @(*) begin
        if (rst == `RstDisable) begin // 有读写请求时
          if(re) begin
          r_data = pmem_read(r_addr);
          end
          else begin
                r_data = 0;
          end
          if (we) begin // 有写请求时
            pmem_write(w_addr, w_data, w_mask);
          end
        end
        else begin
          r_data = 0;
        end
      end

// always @(*) begin
//         if (rst == `RstEnable) begin
//                 r_data = 0;
//         end
//         else begin
//                 if(re == `Enable) begin
//                         r_data = pmem_read(r_addr);
//                 end
//                 else begin
//                         r_data = 0;
//                 end
//         end
// end


// always @(posedge clk) begin
//         if (rst == `RstEnable) begin
//                 // Do nothing
//         end
//         else begin
//                 if(we == `Enable) begin
//                         pmem_write(w_addr, w_data, w_mask);
//                 end
//         end
// end


endmodule


