#ifndef __INST_H_
#define __INST_H_

#include <verilated.h>
#include <Vcpu.h>
#include <common.h>

void load_img();
void print_inst(word_t pc);
void print_all_insts();
#endif // ! __INST_H_
