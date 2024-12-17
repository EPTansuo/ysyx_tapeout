
import "DPI-C" function void npc_ebreak();
import "DPI-C" function void inst_invalid();
import "DPI-C" function void axi_error(input byte errno, input byte isRead);//读错误还是写错误

module InstInvalid(
    input isvalid
);

always @(*) begin
    if(!isvalid)begin
            inst_invalid();
    end
end

endmodule


module Ebreak(
    input isebreak
);

always @(*) begin
    if(isebreak)begin
            npc_ebreak();
    end
end

endmodule

module AXIError(
    input [1:0] bresp,
    input [1:0] rresp,
    input wen,
    input ren
);

always @(*) begin
    if(wen & bresp[1]) begin
        axi_error({6'b0,bresp}, 8'b0);
    end

    if(ren & rresp[1]) begin
        axi_error({6'b0,rresp}, 8'b1);
    end
end

endmodule
