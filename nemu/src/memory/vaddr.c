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
#include <memory/paddr.h>

#ifdef CONFIG_TARGET_SHARE
uint8_t *guest_to_host_mrom(paddr_t paddr);
#endif // !CONFIG_TARGET_SHARE

word_t vaddr_ifetch(vaddr_t addr, int len) {
#ifndef CONFIG_TARGET_SHARE 
  return paddr_read(addr, len);
#else
  if(addr < 0x20000000)
    return paddr_read(addr, len);
  else
    return *(word_t *)guest_to_host_mrom(addr);
#endif 
}


word_t vaddr_read(vaddr_t addr, int len) {
#ifndef CONFIG_TARGET_SHARE 
  return paddr_read(addr, len);
#else
  if(addr < 0x20000000)
    return paddr_read(addr, len);
  else
    return *(word_t *)guest_to_host_mrom(addr);
#endif 
}

void vaddr_write(vaddr_t addr, int len, word_t data) {
  paddr_write(addr, len, data);
}
