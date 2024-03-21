VERILATOR = verilator
SIM_TOP = example
SIM_BUILD_DIR = obj_dir
SIM_EXECUTABLE = $(SIM_BUILD_DIR)/V$(SIM_TOP)
CPP_SRC = csrc/main.cpp
VERILOG_SRC = vsrc/$(SIM_TOP).v
VERILATOR_FLAGS = --cc --exe --trace
VCD_VIEWER = gtkwave
VCD_WAVE = wave.vcd


all: $(CPP_SRC) $(VERILOG_SRC)
	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILOG_SRC) --build --Mdir $(SIM_BUILD_DIR) $(CPP_SRC)
	@echo "Build successfully!"



sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@$(SIM_EXECUTABLE)
	@$(VCD_VIEWER) $(VCD_WAVE)
	
	

clean:
	rm -rf obj_dir $(VCD_WAVE)
	
include ../Makefile




