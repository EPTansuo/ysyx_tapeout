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
#include <color.h>

#include <verilator.h>

static CPU_state cpu_state_buf = {}; 
static int state_index = 0;
static bool first = true;
#define INDEX_INC do{state_index = (state_index + 1) % 2;}while(0)

extern const char *regs[];

// 为了匹配，所以让ref_r使用cpu_state_buf推迟了一个周期
bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  int i =0;
  bool succ = true;
  if(!first){
    
    //if(ref_r->pc != npc_cpu.pc){
    for(i = 0; i < 16; i++){
      if(cpu_state_buf.gpr[i] != npc_cpu.gpr[i]){
        succ = false;
        break;
      }
    }
     if(!succ){

      
      printf("ref reg info:\n");
      for (int j = 0; j < MUXDEF(CONFIG_RVE,16,32); j++)
      {
              if(j==i)
                printf("$%s = 0x%s"  FMT_WORD_HEX_WIDTH COLOR_NONE "%s\t", regs[j], L_RED, cpu_state_buf.gpr[j], COLOR_NONE);
              else
                printf("$%s = 0x" FMT_WORD_HEX_WIDTH "\t", regs[j], cpu_state_buf.gpr[j]);
              if ((j + 1) % 4 == 0)
                      putchar('\n');
      }
      printf("$pc = 0x" FMT_WORD_HEX_WIDTH "\n", cpu_state_buf.pc);
    }


    // if(succ && cpu_state_buf.pc != npc_cpu.gpr[i]){
    //     succ = false;
    //     printf(L_RED "npc: pc = 0x" FMT_WORD_HEX_WIDTH "\tnemu: pc = 0x" FMT_WORD_HEX_WIDTH "\n" COLOR_NONE,npc_cpu.pc,cpu_state_buf.pc);
    //   }
    if(cpu_state_buf.csr.mepc != npc_cpu.csr.mepc){
      printf(L_RED "npc: mepc   = 0x" FMT_WORD_HEX_WIDTH "\tnemu: mepc   = 0x" FMT_WORD_HEX_WIDTH "\n" COLOR_NONE,npc_cpu.csr.mepc,cpu_state_buf.csr.mepc);
      succ = false;
    }
    if(cpu_state_buf.csr.mcause != npc_cpu.csr.mcause){
      printf(L_RED "npc: mcause = 0x" FMT_WORD_HEX_WIDTH "\tnemu: mcause = 0x" FMT_WORD_HEX_WIDTH "\n" COLOR_NONE,npc_cpu.csr.mcause,cpu_state_buf.csr.mcause);
      succ = false;
    }
    if(cpu_state_buf.csr.mstatus != npc_cpu.csr.mstatus){
      printf(L_RED "npc: mstatus= 0x" FMT_WORD_HEX_WIDTH "\tnemu: mstatus= 0x" FMT_WORD_HEX_WIDTH "\n" COLOR_NONE,npc_cpu.csr.mstatus,cpu_state_buf.csr.mstatus);
      succ = false;
    }
    if(cpu_state_buf.csr.mtvec != npc_cpu.csr.mtvec){
      printf(L_RED "npc: mtvec  = 0x" FMT_WORD_HEX_WIDTH "\tnemu: mtvec  = 0x" FMT_WORD_HEX_WIDTH "\n" COLOR_NONE,npc_cpu.csr.mtvec,cpu_state_buf.csr.mtvec);
      succ = false;
    }

   
  }
  if(!succ){
      printf("\e[1;31m Difftest ERROR!\e[0m\n ");
      printf("DO NOT SEE CURRENT INSTRATION, SEE PREVIOUS ONE!\n");
  }
  first = false;
  memcpy(&cpu_state_buf, ref_r, DIFFTEST_REG_SIZE);
 // printf("npc:nemu:ref_r->pc==0x%x\n",ref_r->pc);
  return succ;
}

void isa_difftest_attach() {
}
