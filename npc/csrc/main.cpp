#include <verilated.h>
#include <iostream>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <stdlib.h>
#include "../build/obj_dir/Vcpu.h"
#include "Vcpu___024root.h"
#include "Vcpu_inst_rom.h"
#include "Vcpu_ifu.h"
#include "Vcpu_cpu.h"

#define RESET_ENABLE 1
#define RESET_DISABLE 0

Vcpu *top = new Vcpu; 

// addi x1 x0 1  ; x1 = 1
// addi x2 x0 2  ; x2 = 2 
// addi x2 x1 9  ; x2 = x1 + 9
static uint32_t img[] = {
	0b00000000000100000000000010010011, // 0b000000000001 00000 000 00001 0010011,
	0b00000000001000000000000100010011, // 0b000000000010 00000 000 00010 0010011
	0b0000000100100010000000010010011, // 0b00000 001001 00010 000 00001 0010011
};

uint8_t *init_insts(){
	
	uint8_t *insts = new uint8_t[sizeof(img)];

	size_t inst_num = sizeof(img)/sizeof(uint32_t);

	for (size_t i = 0; i < inst_num; i++)
	{
		for(size_t j=0; j < 4; j++){
			insts[i*4+j] = img[i] >> (j*8) & 0xff;
		}
	}
	
	return insts;
}

void single_cycle(){
	top->clk = 0;
	top->eval();
	top->clk = 1;
	top->eval();
}

void reset(int n){
	top->rst = RESET_ENABLE;
	for (int i = 0; i < n; i++){
		single_cycle();
	}
	top->rst = RESET_DISABLE;
}


void verilator_sim(int argc, char **argv)
{
	Verilated::commandArgs(argc, argv);

	Verilated::traceEverOn(true);

	VerilatedContext *contextp = new VerilatedContext;

	

	VerilatedVcdC *tfp = new VerilatedVcdC;

	top->trace(tfp, 0);
	tfp->open("wave.vcd");
	reset(10);

	Vcpu_ifu* ifu1 = top->cpu->ifu1;
	Vcpu_inst_rom* inst_rom1 = ifu1->inst_rom1;
	uint8_t* new_insts = init_insts();

	// 获取对 inst_rom1 实例的 insts 数组的引用
	VlUnpacked<unsigned char, 131072>& insts = inst_rom1->insts;
	size_t inst_num = sizeof(img)/sizeof(uint32_t);
	// 使用循环来复制数据
	for (size_t i = 0; i < inst_num; ++i) {
	insts[i] = new_insts[i];
	}

	// 释放 new_insts 数组，如果它是动态分配的
	delete[] new_insts;

	//std::cout<<top->rootp->inst;
	//top->inst_rom1->insts = init_insts();
	tfp->dump(contextp->time());
	for (int i = 0; i < 3; i++)
	{
		single_cycle();
		contextp->timeInc(1);
		tfp->dump(contextp->time());
		//contextp->timeInc(1);
	}

	top->final();
	tfp->close();

	delete top;
}


int main(int argc, char **argv)
{

	verilator_sim(argc, argv);
}
