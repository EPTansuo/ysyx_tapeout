#include "ftrace.h"
#include <stdio.h>


int main(){
    char filename[] = {"/home/han/Disk/Document/PROJECT/ysyx/ysyx-workbench/am-kernels/kernels/psram-test/build/psram-test-riscv32e-ysyxsoc.elf"};

    ftrace_init(filename);
    //ftrace_print_func_list();
    uint32_t pc;
    char buf[100];
    while(1){
        scanf("%x", &pc);
        ftrace_get_func_name(buf, pc);
        printf("%s\n", buf);
        fflush(stdout);
    }
    return 0;

}   
