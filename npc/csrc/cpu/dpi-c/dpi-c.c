#include <cpu.h>
#include "../../include/utils.h"
#include <Vcpu.h>
#include <Vcpu_cpu.h>
#include <Vcpu_pc.h>
#include <Vcpu_ifu.h>
#include <Vcpu_gpr.h>
#include <color.h>
#include <Vcpu_inst_rom.h>
#include <inst.h>
#include "Vcpu__Dpi.h"

extern Vcpu* top;

void npc_ebreak(){
	NPCTRAP(top->cpu->pc1->pc, top->cpu->gpr1->regs[10]);
}


void inst_invalid(){
	char logbuf[50];
	word_t pc = top->cpu->pc1->pc;
	printf(L_RED "Invalid or Unimplemented Inst" NONE "\n");
	print_inst(top->cpu->ifu1->inst_rom1->insts, pc);
	//disassemble(logbuf, 40, pc, (uint8_t *)(&_img[pc-0x80000000]), 4);
	disassemble(logbuf, 50, pc );  //top->cpu->ifu1->inst_rom1->insts[pc-0x80000000]);
	printf("\t%s\n",logbuf);
	set_npc_state(NPC_ABORT, top->cpu->pc1->pc, -1);
}