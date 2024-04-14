#ifndef __REG_H_
#define __REG_H_
#include <stdint.h>
#include <verilated.h>

#define CONFIG_RV32 1


#ifdef CONFIG_RV64
typedef uint64_t word_t;
typedef int64_t sword_t;
#else
typedef uint32_t word_t;
typedef int32_t sword_t;
#endif



const char* reg_name(int idx);
int check_reg_idx(int idx);
void isa_reg_display(const VlUnpacked<IData/*31:0*/, 32> & gpr, word_t pc);

#endif 
