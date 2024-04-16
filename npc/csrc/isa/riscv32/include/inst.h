#ifndef __INST_H_
#define __INST_H_

#include <verilated.h>
#include <Vcpu.h>

void init_insts(const char* img_file, VlUnpacked<unsigned char, 131072>& insts);
void init_insts(VlUnpacked<unsigned char, 131072>& insts);
void print_inst(const VlUnpacked<unsigned char, 131072>& insts, word_t pc);
void print_insts(Vcpu* top);
#endif // ! __INST_H_
