
import "DPI-C" function void npc_ebreak();
import "DPI-C" function void inst_invalid();

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
