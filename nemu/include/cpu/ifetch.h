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

#ifndef __CPU_IFETCH_H__

#include <memory/vaddr.h>

#ifdef CONFIG_TARGET_SHARE
uint8_t *guest_to_host_mrom(paddr_t paddr);
#endif // !CONFIG_TARGET_SHARE

static inline uint32_t inst_fetch(vaddr_t *pc, int len) {
#ifdef CONFIG_TARGET_SHARE
  uint32_t inst = *guest_to_host_mrom(*pc);
  printf("inst_fetch: pc = 0x%x, inst = 0x%x\n", *pc, inst);
#else // !CONFIG_TARGET_SHARE
  uint32_t inst = vaddr_ifetch(*pc, len);
#endif 
  (*pc) += len;
  return inst;
}

#endif 