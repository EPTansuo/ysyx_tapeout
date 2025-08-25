sim-iverilog: verilog bin2mem
	iverilog  iverilog/tb.v build/ysyx_23060246.v -D__NPC__ -DIMG_PATH$(IMG).mem" -o $(abspath $(BIN))
	$(BIN)


bin2mem:
	xxd -p -c1 $(IMG) > $(IMG).mem



