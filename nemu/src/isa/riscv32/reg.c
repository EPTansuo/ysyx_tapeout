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
#include "local-include/reg.h"
#include <stdio.h>
#include <fmt-def.h>

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() {
	printf("reg info:\n");
  int reg_num = MUXDEF(CONFIG_RVE, 16, 32);
  for (int i = 0; i < reg_num; i++) {
        printf("$%s = 0x"FMT_WORD_HEX_WIDTH"\t", regs[i], gpr(i));
        if((i+1)%4 == 0)
          putchar('\n');
  }
  printf("$pc = 0x" FMT_WORD_HEX_WIDTH "\n", cpu.pc);
}

word_t isa_reg_str2val(const char *s, bool *success) {
  if(!strcmp(s, "$pc"))
  {
    *success = true;
    return cpu.pc;
  }

  if(!strcmp(s, "$0"))  //这里匹配$0,后面的for循环代码还可以匹配$$0
  {
    *success = true;
    return gpr(0);    
  }

  int len = MUXDEF(CONFIG_RVE, 16, 32);
  for(int i=0; i<len; i++){
    if(strcmp(s+1, regs[i])==0){   //这里地址+1, 例如： $s0 去匹配 s0，要去掉前面的$
      *success = true;
      return gpr(i);
    }
  }
  *success = false;
  return 0;
}
