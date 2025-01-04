#include <common.h>
#include <fmt-def.h>

void btrace_init(){
    printf("BTRACE: %s\n", ANSI_FMT("ON", ANSI_FG_GREEN));
}

void btrace(word_t pc, uint32_t inst, bool taken){
  static bool isfirst = true;
  if(isfirst){ isfirst = false; btrace_init(); return;}
  
  printf("BTRACE: pc: " FMT_WORD_HEX_WIDTH " inst: 0x%08x taken: %d\n", pc, inst, taken);
}
