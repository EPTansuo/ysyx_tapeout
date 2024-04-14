#include "reg.h"
#include <stdio.h>
#include <assert.h>
#include "fmt-def.h"
#include <verilated.h>

const char *regs[] = {
    "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
    "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
    "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
    "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"};

inline int check_reg_idx(int idx){
        if (idx < 0 || idx >= 32)
        {
                printf("Invalid register index %d\n", idx);
                assert(0);
        }
        return idx;
}

inline const char *reg_name(int idx){
        return regs[check_reg_idx(idx)];
}

void isa_reg_display(const VlUnpacked<IData/*31:0*/, 32> & gpr, word_t pc){
        printf("reg info:\n");
        int reg_num = 32;
        for (int i = 0; i < reg_num; i++)
        {
                printf("$%s = 0x" FMT_WORD_HEX_WIDTH "\t", regs[i], (word_t)gpr[i]);
                if ((i + 1) % 4 == 0)
                        putchar('\n');
        }
        printf("$pc = 0x" FMT_WORD_HEX_WIDTH "lx\n", pc);
}