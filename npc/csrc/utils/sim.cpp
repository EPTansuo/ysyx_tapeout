#include <sim.h>
#include <verilator.h>
#include <common.h>
#include <nvboard.h>

void nvboard_bind_all_pins(VysyxSoCFull* top);
#ifdef CONFIG_WAVE_VCD
VerilatedVcdC *tfp = NULL;
#endif
#ifdef CONFIG_WAVE_FST
VerilatedFstC *ftp = NULL;
#endif

VerilatedContext *contextp = NULL;
VysyxSoCFull* top = NULL;

void init_sim(){
	top = new VysyxSoCFull;
	nvboard_bind_all_pins(top);
	nvboard_init();
	top->clock = 0;
#ifdef CONFIG_WAVE_DUMP
    Verilated::traceEverOn(true);
	tfp = MUXDEF(CONFIG_WAVE_VCD, new VerilatedVcdC, new VerilatedFstC);
	contextp = new VerilatedContext;
    top->trace(tfp, 0);
	tfp->open(MUXDEF(CONFIG_WAVE_VCD,"wave.vcd","wave.fst"));
#endif 
	
	
}

void stop_sim(){
    top->final();
#ifdef CONFIG_WAVE_DUMP
	tfp->close();
#endif 
 	
}