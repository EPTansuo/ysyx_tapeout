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

// this is not consistent with uint8_t
// but it is ok since we do not access the array directly
/*static const uint32_t img [] = {
  0x00000297,  // auipc t0,0
  0x00028823,  // sb  zero,16(t0)
  0x0102c503,  // lbu a0,16(t0)
  0x00100073,  // ebreak (used as nemu_trap)
  0xdeadbeef,  // some data
};*/


static const uint32_t img[] = {
  0x00000297,  // auipc t0, 0 - Set t0 to current PC + 0
  0x00428293,  // addi t0, t0, 4 - Add 4 to t0 and store the result in t0
  0x00830313,  // addi t1, t0, 8 - Add 8 to t0 and store the result in t1
  0x00C38393,  // addi t2, t1, 12 - Add 12 to t1 and store the result in t2
  // Now t0 = PC + 4, t1 = PC + 12, t2 = PC + 24
  0x01040413,  // addi s0, t2, 16 - Add 16 to t2 and store the result in s0
  // Now s0 = PC + 40
  0x01448493,  // addi s1, s0, 20 - Add 20 to s0 and store the result in s1
  // Now s1 = PC + 60
};





static void restart() {
  /* Set the initial program counter. */
  cpu.pc = RESET_VECTOR;

  /* The zero register is always 0. */
  cpu.gpr[0] = 0;
}

void init_isa() {
  /* Load built-in image. */
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));

  /* Initialize this virtual computer system. */
  restart();
}
