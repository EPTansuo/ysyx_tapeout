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

static CPU_state cpu_state_buf = {}; 
static int state_index = 0;
static bool first = true;
#define INDEX_INC do{state_index = (state_index + 1) % 2;}while(0)

extern const char *regs[];

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  bool succ = true;
  if(!first){
    //if(ref_r->pc != npc_cpu.pc){
    if(false){
      succ = false;
    }
    else {
      for(int i = 0; i < 32; i++){
        if(cpu_state_buf.gpr[i] != npc_cpu.gpr[i]){
          succ = false;
          break;
        }
      }
    }

    //  printf("npc:nemu:pc==0x%x\n",cpu_state_buf.pc);
    //  printf("ref reg info:\n");
    //     int reg_num = 32;
    //     for (int i = 0; i < reg_num; i++)
    //     {
    //             printf("$%s = 0x" FMT_WORD_HEX_WIDTH "\t", regs[i], cpu_state_buf.gpr[i]);
    //             if ((i + 1) % 4 == 0)
    //                     putchar('\n');
    //     }
    //     printf("$pc = 0x" FMT_WORD_HEX_WIDTH "\n", top->cpu->pc1->pc);
    if(!succ){
      printf("\e[1;31m Difftest ERROR!\e[0m\n ");
      printf("DO NOT SEE CURRENT INSTRATION, SEE PREVIOUS ONE!\n");
      return false;
    }
  }
  first = false;
  memcpy(&cpu_state_buf, ref_r, DIFFTEST_REG_SIZE);
 // printf("npc:nemu:ref_r->pc==0x%x\n",ref_r->pc);
  return succ;
}

void isa_difftest_attach() {
}
