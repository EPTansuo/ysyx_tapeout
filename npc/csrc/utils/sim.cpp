#include <sim.h>
#include <verilator.h>
#include <common.h>
#include <nvboard.h>

void nvboard_bind_all_pins(VysyxSoCFull* top);
#ifdef CONFIG_WAVE_VCD
VerilatedVcdC *tfp = NULL;
#endif
#ifdef CONFIG_WAVE_FST
VerilatedFstC *tfp = NULL;
#endif

VerilatedContext *contextp = NULL;
VysyxSoCFull* top = NULL;

void init_sim(int argc, char** argv){
	Verilated::commandArgs(argc, argv);
	printf("command arg\n");
	top = new VysyxSoCFull;
	nvboard_bind_all_pins(top);
	nvboard_init();
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