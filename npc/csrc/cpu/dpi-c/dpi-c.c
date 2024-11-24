#include <cpu/cpu.h>
#include <utils/utils.h>
#include <color.h>
#include <inst.h>
#include <fmt-def.h>
#include <memory/host.h>
#include <time.h>
#include <iostream>

#include <verilator.h>


extern unsigned char isa_logo[];

uint8_t* guest_to_host(paddr_t paddr);
paddr_t host_to_guest(uint8_t *haddr);

uint64_t npc_uptime;


extern "C" int get_inst(int pc){
  return pmem_read(pc);
}


extern "C" void npc_ebreak(){
	NPCTRAP(PC, REGS[10]);
}


extern "C" void inst_invalid(){
	if(npc_state.state == NPC_ABORT)
		return;
	char logbuf[64];
	word_t pc = PC;
	printf(L_RED "%s" COLOR_NONE "\n", isa_logo);
	printf(L_RED "Invalid or Unimplemented Inst" COLOR_NONE "\n");
  printf("0x" FMT_WORD_HEX_WIDTH ":    ", PC);
  disassemble(logbuf, 64, PC , guest_to_host(PC), 4);
  
  
  for(int j = 3; j >= 0; j--){
    printf("%02x ", ((uint8_t*)guest_to_host(PC))[j]);
  }


  printf("%s\n", logbuf);
	set_npc_state(NPC_ABORT, PC, -1);
}

uint64_t get_rtc_time(){
  time_t t = time(NULL);
  return t;
}


extern "C" int pmem_read(int raddr){

#ifdef CONFIG_HAS_TIMER
  if(raddr == CONFIG_RTC_MMIO) {
    //获取开机时间
    return (uint32_t)get_time();
  }
  else if (raddr == CONFIG_RTC_MMIO + 4) {
    return (uint32_t)(get_time() >> 32);
  }
#endif
  
  if(raddr < CONFIG_MBASE || raddr > CONFIG_MBASE + CONFIG_MSIZE)
    return 0;
  word_t data = host_read(guest_to_host(raddr), 4);
  //printf("read 4 bytes at 0x%x, data = 0x%x\n", raddr, data);
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

extern "C" void pmem_write(int waddr, int wdata, char wmask){
  static memwrite_info mwinfo;   //防止多次输出
  
  if(mwinfo.pc != PC || mwinfo.addr != waddr
      || mwinfo.wmask != wmask || mwinfo.data != wdata 
      || (!regs_equ(REGS,mwinfo.regs))){

#ifdef CONFIG_HAS_SERIAL
      if(waddr == CONFIG_SERIAL_MMIO) {
          //printf(L_PURPLE "%c" COLOR_NONE "", wdata);
          putchar(wdata);
          fflush(stdout);      
          //setbuf(stdout,NULL);
          //printf("%c",wdata);
          // putc(wdata,stdout);
          goto end_pmem_write;
      }
      else 
#endif 
	  if (waddr > CONFIG_MBASE + CONFIG_MSIZE){
        goto end_pmem_write;
      }
      
      #ifdef CONFIG_MTRACE
      printf("--------MTRACE---------\n");
      printf("wmask = 0x%x\n", wmask);
      print_memwrite(waddr, wmask == 0x01 ? 1 : wmask == 0x03 ? 2 : wmask ==0x0f ? 4 : 0, wdata);
      #endif

      switch (wmask)
      {
        case 0x01: host_write(guest_to_host(waddr), 1, wdata); 
                   // printf("write 1 byte at 0x%x, data = 0x%x\n", waddr, wdata);
                   break; 
        case 0x03: host_write(guest_to_host(waddr), 2, wdata);
                   // printf("write 2 bytes at 0x%x, data = 0x%x\n", waddr, wdata);
                    break;
        case 0x0f: host_write(guest_to_host(waddr), 4, wdata);
                   // printf("write 4 bytes at 0x%x, data = 0x%x\n", waddr, wdata);
                    break;
      default:
        printf( L_RED " Can only write for 1/2/4 btyes ()." COLOR_NONE "\n");
        break;
      }
end_pmem_write:
      mwinfo.pc = PC;
      mwinfo.addr = waddr;
      mwinfo.wmask = wmask;
      mwinfo.data = wdata;
      mwinfo.regs = REGS;
  }


}


