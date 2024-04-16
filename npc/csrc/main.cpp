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
#include "Vcpu__Dpi.h"
#include "Vcpu_gpr.h"
#include <iomanip> 
#include <fmt-def.h>
#include <color.h>
#include <reg.h>
#include <unistd.h>
#include <monitor.h>
#include <libgen.h> // 引入libgen库
#include <cpu.h>


#define MAX_CYCLE 200


Vcpu *top;

static  char _img[131072];

bool stop = false;
uint64_t ret_val = 0;
uint32_t inst_num = 0;

//const char * img_file = NULL;

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

void print_insts(Vcpu* top){
	std::cout<<"------------------"<<std::endl;

	for (size_t i = 0; i < inst_num; ++i) {
		print_inst(top->cpu->ifu1->inst_rom1->insts, 0x80000000 + i*4);
	}
	std::cout<<"------------------"<<std::endl;
}



void npc_ebreak(){
	if(stop) return;
	stop = true;
}

void inst_invalid(){
	if(stop) return;
	char logbuf[50];
	word_t pc = top->cpu->pc1->pc;
	std::cout<<L_RED "Invalid or Unimplemented Inst" NONE<<std::endl;
	print_inst(top->cpu->ifu1->inst_rom1->insts, pc);
	//disassemble(logbuf, 40, pc, (uint8_t *)(&_img[pc-0x80000000]), 4);
	disassemble(logbuf, 50, pc );  //top->cpu->ifu1->inst_rom1->insts[pc-0x80000000]);
	printf("\t%s\n",logbuf);
	stop = true;
	//exit(-1);
}

int verilator_sim(int argc, char **argv)
{
	Verilated::commandArgs(argc, argv);

	Verilated::traceEverOn(true);

	VerilatedVcdC *tfp = new VerilatedVcdC;
	VerilatedContext *contextp = new VerilatedContext;

	top->trace(tfp, 0);
	tfp->open("wave.vcd");
	


	Vcpu_inst_rom* inst_rom1 = top->cpu->ifu1->inst_rom1;
	Vcpu_gpr* gpr1 = top->cpu->gpr1;

	if(get_img_file() == NULL)
	//if(true)
		init_insts(inst_rom1->insts);
	else
		init_insts(get_img_file(), inst_rom1->insts);
	
	init_disasm(get_img_file());


	//print_insts(top);

	init_cpu_exec(top, tfp, contextp);
	for (int i = 0; i < MAX_CYCLE && ! stop; i++)
	{
		cpu_single_cycle();

		//isa_reg_display(gpr1->regs, top->cpu->pc1->pc); 
		//print_regs_info();
		//contextp->timeInc(1);
	}
	cpu_eval_dump();

	Vcpu_pc *pc = top->cpu->pc1;

	//std::cout<< "Ebreak at pc: "<<pc->pc<<"\t Inst: ";
	//print_inst(inst_rom1->insts, pc->pc);
	if(!stop){
		std::cout << YELLOW "Maximum cycle reached " NONE;
		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
		top->final();
		tfp->close();
		delete top;
		return (-1);
	}
	else{
		if(gpr1->regs[10] == 0)
		std::cout<<L_GREEN "HIT GOO TRAP " NONE;
		else 
			std::cout<<L_RED "HIT BAD TRAP " NONE;
		printf("at pc: 0x" FMT_WORD_HEX_WIDTH "\n", pc->pc);
	}
	word_t ret_val = (gpr1->regs[10]);
	top->final();
	tfp->close();
	delete top;
	return ret_val;
}


int main(int argc, char **argv)
{
	init_monitor(argc, argv);
	return verilator_sim(argc, argv);
}
