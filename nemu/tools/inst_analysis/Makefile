TARGET=instAnaly
SRCS=instAnaly.c 
LDFLAGS= -lz
CFLAGS=-Wall -O3

$(TARGET):$(SRCS)
	gcc $(LDFLAGS) $(CFLAGS) -o $@ $<


run: $(TARGET)
	./$(TARGET) pc_trace.bin.gz microbench-riscv32-nemu.elf.disasm

tiny_run: $(TARGET)
	./$(TARGET) pc_trace.bin_sum.gz sum-riscv32-nemu.elf.disasm

clean:
	-rm $(TARGET)


