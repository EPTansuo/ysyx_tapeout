#ifndef __REG_H_
#define __REG_H_
#include <stdint.h>
#include <verilated.h>
#include <common.h>


const char* reg_name(int idx);
int check_reg_idx(int idx);
void isa_reg_display(const VlUnpacked<IData/*31:0*/, 32> & gpr, word_t pc);
void print_regs_info();
#endif 
