#ifndef __REG_H_
#define __REG_H_
#include <stdint.h>
#include <verilated.h>
#include <common.h>
#include <Vcpu.h>
#include <Vcpu_cpu.h>
#include <Vcpu_gpr.h>
#include <Vcpu_pc.h>
#include <string.h>


void isa_reg_display();

void print_regs_info();

static inline int check_reg_idx(int idx){
        if (idx < 0 || idx >= 32)
        {
                printf("Invalid register index %d\n", idx);
                assert(0);
        }
        return idx;
}

#define gpr(idx) (top->cpu->gpr1->regs[check_reg_idx(idx)])

static inline const char* reg_name(int idx) {
  extern const char* regs[];
  return regs[check_reg_idx(idx)];
}

#endif 
