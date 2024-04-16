#ifndef __CPU_H_
#define __CPU_H_


#include <common.h>

#include <Vcpu.h>
#include <verilated_vcd_c.h>
#include <verilated.h>

void cpu_exec(uint64_t n);
void print_iringbuf();
void set_nemu_state(int state, vaddr_t pc, int halt_ret);
void invalid_inst(vaddr_t thispc);

void init_cpu_exec(Vcpu* _top, VerilatedVcdC* _tfp, VerilatedContext* _contextp);
void cpu_eval_dump();
void cpu_single_cycle();
void cpu_reset(int n);

#define NEMUTRAP(thispc, code) set_nemu_state(NEMU_END, thispc, code)


#endif 