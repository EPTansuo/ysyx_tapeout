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
#include <time.h>
#include <verilated.h>
#include <iostream>

extern Vcpu* top;
extern unsigned char isa_logo[];

uint8_t* guest_to_host(paddr_t paddr);
paddr_t host_to_guest(uint8_t *haddr);

uint64_t npc_uptime;

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

uint64_t get_rtc_time(){
  time_t t = time(NULL);
  return t;
}


int pmem_read(int raddr){

  if(raddr == CONFIG_RTC_MMIO) {
    //获取开机时间
    return (uint32_t)get_time();
  }
  else if (raddr == CONFIG_RTC_MMIO + 4) {
    return (uint32_t)(get_time() >> 32);
  }
  //printf("readmem at addr :%x \n",raddr );
  if(raddr < CONFIG_MBASE || raddr > CONFIG_MBASE + CONFIG_MSIZE)
    return 0;
  word_t data = host_read(guest_to_host(raddr), 4);
  
  return data;
}
void print_memwrite(paddr_t addr, int len, word_t data);

typedef  struct{
  paddr_t addr;
  char wmask;
  word_t data;
  word_t pc;
  VlUnpacked<word_t, 32> regs;
}memwrite_info;





// return true is equ
bool regs_equ(const VlUnpacked<word_t,32>&reg1, const VlUnpacked<word_t,32>&reg2){
  for(int i = 0; i < 32; i++){
    if(reg1[i] != reg2[i])
      return false;
  }
  return true;
}

void pmem_write(int waddr, int wdata, char wmask){
  static memwrite_info mwinfo;   //防止多次输出
  
  if(mwinfo.pc != top->cpu->pc1->pc || mwinfo.addr != waddr
      || mwinfo.wmask != wmask || mwinfo.data != wdata 
      || (!regs_equ(top->cpu->gpr1->regs,mwinfo.regs))){

      if(waddr == CONFIG_SERIAL_MMIO) {
          //printf(L_PURPLE "%c" NONE "", wdata);
          putchar(wdata);
          goto end_pmem_write;
      }
      else if (waddr > CONFIG_MBASE + CONFIG_MSIZE){
        goto end_pmem_write;
      }
      
      #ifdef CONFIG_MTRACE
      printf("--------MTRACE---------\n");
      printf("wmask = 0x%x\n", wmask);
      print_memwrite(waddr, wmask == 0x01 ? 1 : wmask == 0x03 ? 2 : wmask ==0x0f ? 4 : 0, wdata);
      #endif

      switch (wmask)
      {
        case 0x01: host_write(guest_to_host(waddr), 1, wdata); break; 
        case 0x03: host_write(guest_to_host(waddr), 2, wdata); break;
        case 0x0f: host_write(guest_to_host(waddr), 4, wdata); break;
      default:
        printf( L_RED " Can only write for 1/2/4 btyes ()." NONE "\n");
        break;
      }
end_pmem_write:
      mwinfo.pc = top->cpu->pc1->pc;
      mwinfo.addr = waddr;
      mwinfo.wmask = wmask;
      mwinfo.data = wdata;
      mwinfo.regs = top->cpu->gpr1->regs;
  }


}


