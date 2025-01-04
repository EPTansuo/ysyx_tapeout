#include <common.h>
#include <fmt-def.h>


void btrace(word_t pc, uint32_t inst, bool taken){
  printf("BTRACE: pc: " FMT_WORD_HEX_WIDTH " inst: 0x%08x taken: %d\n", pc, inst, taken);
}
