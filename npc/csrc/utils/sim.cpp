#include <sim.h>
#include <verilator.h>
#include <common.h>

VerilatedVcdC *tfp = NULL;
VerilatedContext *contextp = NULL;
VysyxSoCFull* top = NULL;

void init_sim(){
	top = new VysyxSoCFull;
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