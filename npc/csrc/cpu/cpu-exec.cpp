#include <cpu.h>


static Vcpu* top;
static VerilatedVcdC * tfp;
static VerilatedContext* contextp;


void cpu_eval_dump(){
	top->eval();
        if(tfp != NULL){
        	tfp->dump(contextp->time());
		contextp->timeInc(1);
        }
}

void cpu_single_cycle(){
	int i = 2;
	while((i--))
	{
		top->clk = !top->clk;
		cpu_eval_dump();
	}
}

void cpu_reset(int n){
	top->rst = RESET_ENABLE;
	while(n--)cpu_single_cycle();	
	top->rst = RESET_DISABLE;
}


void init_cpu_exec(Vcpu* _top, VerilatedVcdC* _tfp, VerilatedContext* _contextp){
        top = _top;
        tfp = _tfp;
        contextp = _contextp;
        cpu_reset(3);
}