TOPNAME = npc
MAIN = $(TOPNAME).npcMain
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/OBJ_DIR
IMG = "/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/am-kernels/tests/cpu-tests/build/add-riscv32e-npc.bin"
ARGS = -v --diff="/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/nemu/build/riscv32-nemu-interpreter-so"

VERILATOR = verilator
VERILATOR_COVERAGE = verilator_coverage
# # verilator flags
# VERILATOR_FLAGS += -MMD --trace --build -cc --exe \
# 		-O3 --x-assign fast --x-initial fast --noassert -report-unoptflat
#

VERILATOR_FLAGS += -MMD --build -cc --public --trace\
				--x-assign fast --x-initial fast --noassert \
				-I$(YSYX_HOME)/npc/vsrc  --threads 1

# timescale set
VERILATOR_FLAGS += --timescale 1us/1us
VERILATOR_CONFIG = ./scripts/verilator.vlt


vcd ?=
ifeq ($(vcd), 1)
	CXXFLAGS += -DVCD
endif


-include .config

# C flags
INC_PATH +=$(abspath csrc/include)
INC_PATH +=$(abspath csrc/isa/$(CONFIG_ISA)/include)
INC_PATH +=$(abspath csrc/isa/$(CONFIG_ISA)/local-include)

INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS)
CXXFLAGS += $(CFLAGS)
CXXFLAGS += -Wall $(INCFLAGS) -g -DTOP_NAME="\"V$(TOPNAME)\"" -fpermissive
CXXFLAGS += $(shell llvm-config --cxxflags)
LDFLAGS += $(shell  llvm-config --ldflags --libs --system-libs) -fPIE
LDFLAGS += -lreadline

# source file
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v")
VSRCS += $(shell find $(abspath ./build/verilog) -name "*.v")
SCALA_FILE = $(shell find ./src/main -name '*.scala')
CSRCS = $(shell find $(abspath ./csrc) \( -path $(abspath ./csrc/device) -prune \) -o \( -name "*.c" -or -name "*.cc" -or -name "*.cpp" \) -print)

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk



BIN = $(BUILD_DIR)/$(TOPNAME)
NPC_EXEC := $(BIN)

default: $(BIN)

$(BIN): $(CSRCS) verilog

	$(VERILATOR) $(VERILATOR_CONFIG) $(VERILATOR_FLAGS) -top $(TOPNAME) $(CSRCS) $(VSRCS) \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN)) --top-module $(TOPNAME)

verilog: $(SCALA_FILE) sigmap
	@mkdir -p $(BUILD_DIR)/verilog
	./mill -i $(TOPNAME).runMain $(MAIN) -td $(BUILD_DIR)/verilog --emit-modules verilog

sigmap: $(NPC_HOME)/tools/control_gen/control_table.xlsx
	cd $(NPC_HOME)/tools/control_gen/ && python control_gen.py > $(NPC_HOME)/src/main/scala/sigmap.scala


sim: run
run: $(BIN)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$^ $(ARGS) $(IMG)

# menuconfig and autoconf.h
include $(NPC_HOME)/scripts/config.mk

clean:
	-rm -rf $(BUILD_DIR) logs

clean_mill:
	-rm -rf out

clean_all: clean clean_mill

.PHONY: clean clean_all clean_mill srun run sim verilog


include ../Makefile
