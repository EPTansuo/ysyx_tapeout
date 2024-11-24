#include <verilated.h>
#include <verilated_vcd_c.h>
#include <Vcpu.h>
#include <common.h>

VerilatedVcdC *tfp = NULL;
VerilatedContext *contextp = NULL;
Vcpu * top = NULL;

void init_sim(){
	top = new Vcpu;
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