TOPNAME = cpu
NXDC_FILES = constr/cpu.nxdc
IMG = "/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/am-kernels/tests/cpu-tests/build/add-longlong-riscv32e-npc.bin"
ARGS = -v --diff="/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/nemu/build/riscv32-nemu-interpreter-so"

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc --public --trace \
				-O3 --x-assign fast --x-initial fast --noassert \
				-I$(YSYX_HOME)/npc/vsrc 

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
VCD_WAVE = wave.vcd

-include .config

INC_PATH +=$(abspath csrc/include)
INC_PATH +=$(abspath csrc/isa/$(CONFIG_ISA)/include)
INC_PATH +=$(abspath csrc/isa/$(CONFIG_ISA)/local-include)





default:$(BIN) 

$(shell mkdir -p $(BUILD_DIR))

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# project source
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
CSRCS += $(SRC_AUTO_BIND)
#CSRCS += $(NEMU_HOME)/src/utils/disasm.cc


# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += -Wall $(INCFLAGS) -g -DTOP_NAME="\"V$(TOPNAME)\"" -fpermissive #$(shell llvm-config --cxxflags) 
LDFLAGS += -lreadline
#LDFLAGS += $(shell llvm-config --ldflags)
#LDLIBS += $(shell llvm-config --libs)

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk



$(shell python3 $(NPC_HOME)/tools/exu_gen/exu_gen.py > $(NPC_HOME)/vsrc/exu.v)

$(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE) 
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
all: default

run: $(BIN) 
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	bash ./tools/disasm.sh $(IMG) $(BUILD_DIR)
	$^ $(ARGS) $(IMG)
#	echo "$(ARGS)" $(IMG)
#	@$(SIM_EXECUTABLE)
#	@$(VCD_VIEWER) $(VCD_WAVE)
	
sim: run

sync:
	rsync -av $(NPC_HOME)/vsrc/ $(GZIC_SERVER_USER)@$(GZIC_SERVER_IP):$(GZIC_SERVER_NPC_HOME)/vsrc/
rsync: sync

remote_sim: sync EXU_GEN
	ssh $(GZIC_SERVER_USER)@$(GZIC_SERVER_IP) "cd $(GZIC_SERVER_NPC_HOME) && make sim"
	bash ./tools/mountserver.sh
	bash ./tools/ln_rm_vcd.sh

# menuconfig and autoconf.h
include $(NPC_HOME)/csrc/scripts/config.mk


	
clean:
	rm -rf $(BUILD_DIR) $(VCD_WAVE)

.PHONY: default all clean run

include ../Makefile




