TARGET=instAnaly
SRCS=instAnaly.c 
LDFLAGS= -lz
CFLAGS=-Wall -g -O2

$(TARGET):$(SRCS)
	gcc $(LDFLAGS) $(CFLAGS) -o $@ $<


run: $(TARGET)
	./$(TARGET) pc_trace.bin.gz microbench-riscv32-nemu.elf.disasm

clean:
	-rm $(TARGET)


