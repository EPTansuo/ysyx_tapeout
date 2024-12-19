#include <sim.h>
#include <verilator.h>
#include <common.h>
#include <nvboard.h>

#ifdef CONFIG_USE_NVBOARD
void nvboard_bind_all_pins(VTOP_NAME* top);
#endif 

#ifdef CONFIG_WAVE_VCD
VerilatedVcdC *tfp = NULL;
#endif
#ifdef CONFIG_WAVE_FST
VerilatedFstC *tfp = NULL;
#endif

VerilatedContext *contextp = NULL;
VTOP_NAME* top = NULL;

void init_sim(int argc, char** argv){
	//Verilated::commandArgs(argc, argv);
	top = new VTOP_NAME;
#ifdef CONFIG_USE_NVBOARD
	nvboard_bind_all_pins(top);
	nvboard_init();
#endif 
	top->clock = 0;
#ifdef CONFIG_WAVE_DUMP
	Verilated::traceEverOn(true);
	contextp = new VerilatedContext;
	tfp = MUXDEF(CONFIG_WAVE_VCD, new VerilatedVcdC, new VerilatedFstC);
    top->trace(tfp, 0);
	char buf[300];
	sprintf(buf, "%s/%s", getenv("NPC_HOME"), MUXDEF(CONFIG_WAVE_VCD,"wave.vcd","wave.fst"));
	tfp->open(buf);
#endif 
	
	
}

void stop_sim(){
    top->final();
#ifdef CONFIG_WAVE_DUMP
	tfp->close();
#endif 
 	
}