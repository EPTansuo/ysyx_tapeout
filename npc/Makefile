TOPNAME = ysyxSoCFull
NPC_TOPNAME = ysyx_23060246
MAIN = $(NPC_TOPNAME).npcMain
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/OBJ_DIR
IMG = "$(YSYX_HOME)/am-kernels/tests/cpu-tests/build/add-riscv32e-npc.bin"
ARGS = -v --diff="$(NEMU_HOME)/build/riscv32-nemu-interpreter-so"

SCALA_DIR := ./src/main/scala
VERILOG_DIR := $(BUILD_DIR)/verilog

VERILATOR = verilator
VERILATOR_COVERAGE = verilator_coverage
# # verilator flags
# VERILATOR_FLAGS += -MMD --trace --build -cc --exe \
# 		-O3 --x-assign fast --x-initial fast --noassert -report-unoptflat
#

V_INCPATH+=$(abspath $(YSYX_HOME)/npc/vsrc)
V_INCPATH+=$(shell find $(YSYX_HOME)/ysyxSoC/perip -type f -name '*defines*.v' -exec dirname {} \;)
V_INCFLAGS=$(addprefix -I, $(V_INCPATH))
VERILATOR_FLAGS += -MMD --build -cc --public --trace\
				--x-assign fast --x-initial fast --noassert \
				$(V_INCFLAGS) --threads 1

# timescale set
VERILATOR_FLAGS += --timescale 1us/1us --no-timing
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
CXXFLAGS += -Wno-macro-redefined
CXXFLAGS += -Wall $(INCFLAGS) -g -DTOP_NAME="\"V$(TOPNAME)\"" -fpermissive
CXXFLAGS += $(shell llvm-config --cxxflags)
LDFLAGS += $(shell  llvm-config --ldflags --libs --system-libs) -fPIE
LDFLAGS += -lreadline

# source file
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v")
VSRCS += $(shell find $(YSYX_HOME)/ysyxSoC/perip -name "*.v")
VSRCS += $(shell find $(YSYX_HOME)/ysyxSoC/build -name "*.v")
VSRCS += $(shell find $(abspath ./build/verilog) -name "*.v")

SCALA_FILE = $(shell find ./src/main -name '*.scala')

CSRCS = $(shell find $(abspath ./csrc) \( -path $(abspath ./csrc/device) -prune \) -o \( -name "*.c" -or -name "*.cc" -or -name "*.cpp" \) -print)

include $(NPC_HOME)/csrc/device/filelist.mk



BIN = $(BUILD_DIR)/$(TOPNAME)
NPC_EXEC := $(BIN)

default: $(BIN)

$(BIN): $(CSRCS) verilog
	$(VERILATOR) $(VERILATOR_CONFIG) $(VERILATOR_FLAGS) -top $(TOPNAME) $(CSRCS) $(VSRCS) \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN)) --top-module $(TOPNAME)


SCALA_TIME = $(shell find $(SCALA_DIR) -type f -exec stat --format '%Y' {} \; | sort -n | tail -n 1)
$(shell mkdir -p $(BUILD_DIR)/verilog)
VERILOG_TIME = $(shell find $(VERILOG_DIR) -type f -exec stat --format '%Y' {} \; | sort -n | tail -n 1)
VERILOG_TIME := $(if $(VERILOG_TIME),$(VERILOG_TIME),0)

verilog: $(SCALA_FILE) sigmap

	@if [ "$(SCALA_TIME)" -gt "$(VERILOG_TIME)" ]; then \
		echo "Scala directory changed or Verilog directory missing, generating Verilog..."; \
		./mill -i $(NPC_TOPNAME).runMain $(MAIN) -td $(VERILOG_DIR) --emit-modules verilog; \
	else \
		echo "No changes detected in Scala directory, skipping Verilog generation."; \
	fi

SIGMAP_TIME = $(shell find $(SCALA_DIR) -name sigmap.scala -exec stat --format '%Y' {} \; | sort -n | tail -n 1)
SIGMAP_TABLE_TIME = $(shell find $(NPC_HOME)/tools -name control_table.xlsx -exec stat --format '%Y' {} \; | sort -n | tail -n 1)

sigmap: $(NPC_HOME)/tools/control_gen/control_table.xlsx
	@if [ "$(SIGMAP_TABLE_TIME)" -gt "$(SIGMAP_TIME)" ]; then \
		echo "Sigmap (Control Signal) Table Changed, Generating Code..."; \
		cd $(NPC_HOME)/tools/control_gen/ && python control_gen.py > $(NPC_HOME)/src/main/scala/sigmap.scala; \
	else \
		echo "No changes detected in Sigmap (Control Signal) Table"; \
	fi


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

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

include ../Makefile
