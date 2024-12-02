
module UART(
    input clock,
    input en,
    input [7:0]data
);

always@(posedge clock) begin
    if(en) begin
        $write("%c",data);
    end
end

endmodule
