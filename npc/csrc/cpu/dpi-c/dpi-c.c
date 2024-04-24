#include <cpu/cpu.h>
#include <utils/utils.h>
#include <Vcpu.h>
#include <Vcpu_cpu.h>
#include <Vcpu_pc.h>
#include <Vcpu_ifu.h>
#include <Vcpu_gpr.h>
#include <color.h>
#include <Vcpu_inst_rom.h>
#include <inst.h>
#include "Vcpu__Dpi.h"
#include <fmt-def.h>
#include <memory/host.h>

extern Vcpu* top;
extern unsigned char isa_logo[];

uint8_t* guest_to_host(paddr_t paddr);
paddr_t host_to_guest(uint8_t *haddr);


void npc_ebreak(){
	NPCTRAP(top->cpu->pc1->pc, top->cpu->gpr1->regs[10]);
}


void inst_invalid(){
	if(npc_state.state == NPC_ABORT)
		return;
	char logbuf[50];
	word_t pc = top->cpu->pc1->pc;
	printf(L_RED "%s" NONE "\n", isa_logo);
	printf(L_RED "Invalid or Unimplemented Inst" NONE "\n");
	//print_inst( pc);
	//disassemble(logbuf, 40, pc, (uint8_t *)(&_img[pc-0x80000000]), 4);
	disassemble(logbuf, 50, pc );  //top->cpu->ifu1->inst_rom1->insts[pc-0x80000000]);
	printf("At pc = 0x" FMT_WORD_HEX "\t%s\n", top->cpu->pc1->pc,logbuf);
	set_npc_state(NPC_ABORT, top->cpu->pc1->pc, -1);
}




int pmem_read(int raddr){
  return host_read(guest_to_host(raddr), 4);
}
void pmem_write(int waddr, int wdata, char wmask){
  int aligned_addr = waddr & (~0x3u);
  int cur_data = host_read(guest_to_host(aligned_addr), 4);
  for (int i = 0; i < 4; i++) {
      if (wmask & (1 << i)) {
          int shift = i * 8;
          int mask = 0xFF << shift;
          cur_data &= (~mask);
          cur_data |= (wdata & mask);
      }
  }
  host_write(guest_to_host(aligned_addr),4,cur_data);
}
