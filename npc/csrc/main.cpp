#include <verilated.h>
#include <iostream>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <cstdlib>
#include <cstdint>
#include "../build/obj_dir/Vcpu.h"
#include "Vcpu___024root.h"
#include "Vcpu_inst_rom.h"
#include "Vcpu_pc.h"
#include "Vcpu_ifu.h"
#include "Vcpu_cpu.h"
#include "Vcpu__Dpi.h"
#include "Vcpu_gpr.h"
#include <iomanip> 
#include "fmt-def.h"
#include "color.h"
#include "reg.h"


#define RESET_ENABLE 1
#define RESET_DISABLE 0

#define CONFIG_RV32 1

#define MAX_CYCLE 20


Vcpu *top = new Vcpu; 


bool stop = false;
uint64_t ret_val = 0;
uint32_t inst_num = 0;

//const char * img_file = NULL;
const char *img_file = "/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/am-kernels/tests/cpu-tests/build/dummy-riscv32e-npc.bin";

// addi x1 x0 1  ; x1 = 1
// addi x2 x0 2  ; x2 = 2 
// addi x2 x1 9  ; x2 = x1 + 9
static uint32_t img[] = {                   //    imm          rs1       rd   opcode
	0b00000000000100000000000010010011, // 0b000000000001 00000 000 00001 0010011,
	0b00000000001000000000000100010011, // 0b000000000010 00000 000 00010 0010011
	0b00000000100100001000000100010011, // 0b000000001001 00001 000 00010 0010011
	0b00000000000100000000000001110011  // ebreak
};

void init_insts(const char* img_file, VlUnpacked<unsigned char, 131072>& insts){
	unsigned char _img[131072];
	FILE* fp = fopen(img_file,"r");
	if(fp == NULL){
		printf(L_RED "Can not open init insts!\n" NONE);
		return;
	}
	fseek(fp, 0, SEEK_SET);
	inst_num = fread(_img, 1, 131072, fp) / 4;
	for(size_t i = 0; i < 131072; i++){
		insts[i] = _img[i];
	}
	fclose(fp);	
}

void init_insts(VlUnpacked<unsigned char, 131072>& insts){
	
	//uint8_t *insts = new uint8_t[sizeof(img)];

	inst_num = sizeof(img)/sizeof(uint32_t);

	for (size_t i = 0; i < inst_num; i++)
	{
		for(size_t j=0; j < 4; j++){
			insts[i*4+j] = img[i] >> (j*8) & 0xff;
		}
	}
	
	//return insts;
}

void print_inst(const VlUnpacked<unsigned char, 131072>& insts, word_t pc)
{
	word_t index = pc - 0x80000000;
	std::cout << std::hex << std::setw(8) << std::setfill('0')
		<< pc << ":    ";
	std::cout << std::hex << std::setw(2) << std::setfill('0')
		<< static_cast<int>(insts[index+3]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+2]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+1]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+0])
		<< std::endl;
}

void single_cycle(){
	top->clk = 0;
	top->eval();
	top->clk = 1;
	top->eval();
}

void reset(int n){
	top->rst = RESET_ENABLE;
	while(--n)single_cycle();	
	top->rst = RESET_DISABLE;
}


void npc_ebreak(){
	stop = true;
}

void inst_invalid(){
	std::cout<<L_RED "Invalid Inst" NONE<<std::endl;
	print_inst(top->cpu->ifu1->inst_rom1->insts, top->cpu->pc1->pc);
	exit(-1);
}

void verilator_sim(int argc, char **argv)
{
	Verilated::commandArgs(argc, argv);

	Verilated::traceEverOn(true);

	VerilatedVcdC *tfp = new VerilatedVcdC;
	VerilatedContext *contextp = new VerilatedContext;

	top->trace(tfp, 0);
	tfp->open("wave.vcd");
	


	Vcpu_inst_rom* inst_rom1 = top->cpu->ifu1->inst_rom1;
	Vcpu_gpr* gpr1 = top->cpu->gpr1;

	if(img_file == NULL)
		init_insts(inst_rom1->insts);
	else
		init_insts(img_file, inst_rom1->insts);
	

	// std::cout<<"------------------"<<std::endl;
	// for(size_t i = 0; i < inst_num; i++){
	// 	std::cout<<std::hex<< std::setw(8) << std::setfill('0')<<img[i]<<std::endl;
	// }
	std::cout<<"------------------"<<std::endl;

	for (size_t i = 0; i < inst_num; ++i) {
		print_inst(inst_rom1->insts, 0x80000000 + i*4);
	}
	std::cout<<"------------------"<<std::endl;

	//std::cout<<top->rootp->inst;
	//top->inst_rom1->insts = init_insts();
	reset(10);
	tfp->dump(contextp->time());
	for (int i = 0; i < MAX_CYCLE && ! stop; i++)
	{
		
		single_cycle();
		contextp->timeInc(1);
		tfp->dump(contextp->time());
		isa_reg_display(gpr1->regs, top->cpu->pc1->pc); 
		//contextp->timeInc(1);
	}
	contextp->timeInc(1);
	tfp->dump(contextp->time());

	Vcpu_pc *pc = top->cpu->pc1;

	//std::cout<< "Ebreak at pc: "<<pc->pc<<"\t Inst: ";
	//print_inst(inst_rom1->insts, pc->pc);
	if(!stop){
		std::cout << YELLOW "Maximum cycle reached " NONE;
		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
	}
	else{
		if(gpr1->regs[10] == 0)
		std::cout<<L_GREEN "HIT GOO TRAP " NONE;
		else 
			std::cout<<L_RED "HIT BAD TRAP " NONE;
		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
	}
	
	top->final();
	tfp->close();

	delete top;
}


int main(int argc, char **argv)
{

	verilator_sim(argc, argv);
}
