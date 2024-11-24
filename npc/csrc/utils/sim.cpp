#include <sim.h>
#include <common.h>

VerilatedVcdC *tfp = NULL;
VerilatedContext *contextp = NULL;
Vnpc* top = NULL;

void init_sim(){
	top = new Vnpc;
	top->clock = 0;
#ifdef CONFIG_WAVE_DUMP
    Verilated::traceEverOn(true);
	tfp = new VerilatedVcdC;
	contextp = new VerilatedContext;
    top->trace(tfp, 0);
	tfp->open("wave.vcd");
#endif 
	
	
}

void stop_sim(){
        top->final();
#ifdef CONFIG_WAVE_DUMP
	tfp->close();
#endif 
 	
}