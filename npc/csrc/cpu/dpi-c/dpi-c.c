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

  if(raddr < 0x80000000)
    return 0;
  word_t data = host_read(guest_to_host(raddr), 4);
  //printf("pmem_read: raddr = 0x%x, data = 0x%x\n", raddr, data);
  return data;
}

void pmem_write(int waddr, int wdata, char wmask){
  switch (wmask)
  {
    case 0x01: host_write(guest_to_host(waddr), 1, wdata); break; 
    case 0x03: host_write(guest_to_host(waddr), 2, wdata); break;
    case 0x0f: host_write(guest_to_host(waddr), 4, wdata); break;
  default:
    printf( L_RED " Can only write for 1/2/4 btyes ()." NONE "\n");
    break;
  }
}


