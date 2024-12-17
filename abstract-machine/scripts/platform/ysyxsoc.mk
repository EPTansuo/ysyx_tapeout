AM_SRCS := riscv/ysyxsoc/start.S \
           riscv/ysyxsoc/trm.c \
           riscv/ysyxsoc/ioe.c \
           riscv/ysyxsoc/timer.c \
           riscv/ysyxsoc/input.c \
           riscv/ysyxsoc/cte.c \
           riscv/ysyxsoc/trap.S \
           riscv/ysyxsoc/gpu.c \
           riscv/ysyxsoc/uart.c \
           riscv/ysyxsoc/bl.c \
           platform/dummy/vme.c \
           platform/dummy/mpe.c 


CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linker_ysyxsoc_rtt.ld  \
						 --defsym=_pmem_start=0x80000000 --defsym=_entry_offset=0x0

LDFLAGS   += --gc-sections -e _start --print-map
CFLAGS += -DMAINARGS=\"$(mainargs)\"

NPCFLAGS =--diff=$(NEMU_HOME)/build/riscv32-nemu-interpreter-so 

BATCH ?= 0
ifeq ($(BATCH), 1)
    NPCFLAGS += -b
endif


.PHONY: $(AM_HOME)/am/src/riscv/ysyxsoc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	#@$(OBJCOPY) --only-section=.text --only-section=.rodata --only-section=.data --only-section=.bootloader\
	#-only-section=.fsbl -O binary $(IMAGE).elf $(IMAGE).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run:  image
	$(MAKE) -C $(NPC_HOME) run  ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
