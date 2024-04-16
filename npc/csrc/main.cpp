#include <verilated.h>
#include <iostream>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <cstdlib>
#include <cstdint>
#include "Vcpu.h"
#include "Vcpu___024root.h"
#include "Vcpu_inst_rom.h"
#include "Vcpu_pc.h"
#include "Vcpu_ifu.h"
#include "Vcpu_cpu.h"

#include "Vcpu_gpr.h"
#include <iomanip> 
#include <fmt-def.h>
#include <color.h>
#include <reg.h>
#include <unistd.h>
#include <monitor.h>
#include <libgen.h> // 引入libgen库
#include <cpu.h>
#include <inst.h>

#define MAX_CYCLE 200



extern  char img[131072];

bool stop = false;
uint64_t ret_val = 0;
extern uint32_t inst_num;

void engine_start();
int is_exit_status_bad();


// int verilator_sim(int argc, char **argv)
// {
// 	Verilated::commandArgs(argc, argv);

// 	Verilated::traceEverOn(true);

// 	VerilatedVcdC *tfp = new VerilatedVcdC;
// 	VerilatedContext *contextp = new VerilatedContext;

// 	top->trace(tfp, 0);
// 	tfp->open("wave.vcd");
	


// 	Vcpu_inst_rom* inst_rom1 = top->cpu->ifu1->inst_rom1;
// 	Vcpu_gpr* gpr1 = top->cpu->gpr1;




// 	//print_insts(top);

// 	init_cpu_exec(top, tfp, contextp);
// 	for (int i = 0; i < MAX_CYCLE && ! stop; i++)
// 	{
// 		cpu_single_cycle();

// 		//isa_reg_display(gpr1->regs, top->cpu->pc1->pc); 
// 		//print_regs_info();
// 		//contextp->timeInc(1);
// 	}
// 	cpu_eval_dump();

// 	Vcpu_pc *pc = top->cpu->pc1;

// 	//std::cout<< "Ebreak at pc: "<<pc->pc<<"\t Inst: ";
// 	//print_inst(inst_rom1->insts, pc->pc);
// 	if(!stop){
// 		std::cout << YELLOW "Maximum cycle reached " NONE;
// 		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
// 		top->final();
// 		tfp->close();
// 		delete top;
// 		return (-1);
// 	}
// 	else{
// 		if(gpr1->regs[10] == 0)
// 		std::cout<<L_GREEN "HIT GOO TRAP " NONE;
// 		else 
// 			std::cout<<L_RED "HIT BAD TRAP " NONE;
// 		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
// 	}
// 	word_t ret_val = (gpr1->regs[10]);
// 	top->final();
// 	tfp->close();
// 	delete top;
// 	return ret_val;
// }


int main(int argc, char **argv)
{
	init_monitor(argc, argv);
	engine_start();
	return is_exit_status_bad();
	//return verilator_sim(argc, argv);
}
