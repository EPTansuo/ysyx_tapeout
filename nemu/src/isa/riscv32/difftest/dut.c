/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/difftest.h>
#include "../local-include/reg.h"
#include <cpu/cpu.h>
#include <fmt-def.h>

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  bool succ = true;

  ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);

  if (cpu.pc != ref_r->pc) {   //check pc
    succ = false;
    printf("\e[1;31mPC DIFFTESET ERROR!\e[0m\n");
    goto print_error_info;
  }
  else{
    succ = (memcmp(cpu.gpr, ref_r->gpr, DIFFTEST_REG_SIZE) == 0) ; // check gpr
    printf("\e[1;31mGPR DIFFTESET ERROR!\e[0m\n");
    if(!succ) goto print_error_info;
  }

  succ = (cpu.csr.mstatus == ref_r->csr.mstatus) && 
         (cpu.csr.mcause == ref_r->csr.mcause) &&
         (cpu.csr.mepc == ref_r->csr.mepc) && 
         (cpu.csr.mtvec == ref_r->csr.mtvec);
  if(!succ){
     printf("\e[1;31mCSR DIFFTESET ERROR!\e[0m\n");
     goto print_error_info;
  }

  if(succ)
    return true;

print_error_info:
  printf("\e[1;31mDifftest ERROR!\e[0m\n  pc: 0x"FMT_WORD_HEX"\n", pc);
#ifdef CONFIG_ITRACE
  print_iringbuf();
#endif 
  return false;
}

void isa_difftest_attach() {
}
