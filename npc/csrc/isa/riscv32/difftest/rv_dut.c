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
#include <sim.h>

static CPU_state cpu_state[2] = {}; 
static int state_index = 0;
static bool first = true;
#define INDEX_INC do{state_index = (state_index + 1) % 2;}while(0)

extern const char *regs[];

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  bool succ = true;

  ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);

  memcpy(&cpu_state[state_index], &ref_r, sizeof(CPU_state));
  INDEX_INC;

  if(first){
    first = false;
    return true;
  }

  // if (cpu.pc != ref_r->pc) {
  //  succ = false;
  // }
  // else{
    for(int i=0; i<32; i++){
      if(gpr(i) != cpu_state[state_index].gpr[i]){
        succ = false;
        break;
      }
    }
    //succ = (memcmp(cpu.gpr, ref_r->gpr, DIFFTEST_REG_SIZE) == 0) ;
  //}

  if(succ)
    return true;
  
  printf("\e[1;31m Difftest ERROR!\e[0m\n  pc: 0x" FMT_WORD_HEX "\n", pc);
  //print_iringbuf();


  printf("ref reg info:\n");
  int reg_num = 32;
  for (int i = 0; i < reg_num; i++)
  {
          printf("$%s = 0x" FMT_WORD_HEX_WIDTH "\t", regs[i], cpu_state[state_index].gpr[i]);
          if ((i + 1) % 4 == 0)
                  putchar('\n');
  }
  printf("$pc = 0x" FMT_WORD_HEX_WIDTH "\n", cpu_state[state_index].pc);
  printf("Do not compare PC !\n");

  return false;
}

void isa_difftest_attach() {
}
