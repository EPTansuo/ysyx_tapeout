#include "ftrace.h"
#include <stdio.h>


int main(){
    printf("hello\n");
    char filename[] = {"demo.elf"};

    ftrace_init(filename);
    return 0;

}
