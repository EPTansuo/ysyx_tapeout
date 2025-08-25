`ifndef SYNTHESIS
//`ifdef VERILATOR
//`endif
`endif
module InstInvalid(
    input isvalid
);

`ifndef SYNTHESIS
//`ifdef VERILATOR
always @(*) begin
    if(!isvalid)begin
            $display("Inst Invalid!");
						$finish;
    end
end
//`endif
`endif

endmodule


module Ebreak(
    input isebreak
);

`ifndef SYNTHESIS
always @(*) begin
    if(isebreak)begin
						$display("EBreak!");
						$finish;
    end
end
`endif

endmodule

module AXIError(
    input [1:0] bresp,
    input [1:0] rresp,
    input wen,
    input ren
);

`ifndef SYNTHESIS
//`ifdef VERILATOR
//always @(*) begin
//    if(wen & bresp[1]) begin
//        axi_error({6'b0,bresp}, 8'b0);
//    end
//
//    if(ren & rresp[1]) begin
//        axi_error({6'b0,rresp}, 8'b1);
//    end
//end
//`endif
`endif

endmodule
