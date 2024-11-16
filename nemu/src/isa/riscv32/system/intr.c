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

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
 /* *get_isa_csr_str2addr("mepc") = epc;
  *get_isa_csr_str2addr("mcause") = NO;
  return *get_isa_csr_str2addr("mtvec");*/
  // if(NO == 0)
  //   epc += 4;
  
  // //保存中断使能状态，以便后续恢复
  // cpu.csr.mstatus &= ~(1U<<7);     //清除第七位
  // cpu.csr.mstatus |= ((cpu.csr.mstatus&(1<<3))<<4);  //将第三位(MIE)移到第七位(MPIE)

  // //清除MIE，禁用中断，防止中断嵌套
  // cpu.csr.mstatus &= ~(1U<<3);     //清除第三位(MIE)

  // //设置MPP为11，表示中断模式
  // cpu.csr.mstatus |= (3U<<11);     //将 mstatus 的第11位（MPP位之一）和第12位（MPP位之二）置一
  
  // cpu.csr.mcause = NO;
  // cpu.csr.mepc = epc;

  // return cpu.csr.mtvec;


  cpu.csr.mcause = 11; // ecall from M-mode
  cpu.csr.mepc = epc;
  cpu.csr.mstatus = 0x1800;
  return cpu.csr.mtvec;
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
