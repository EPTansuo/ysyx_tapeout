#include <verilated.h>
#include <iostream>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <cstdlib>
#include <cstdint>
#include <iomanip> 
#include <fmt-def.h>
#include <color.h>
#include <reg.h>
#include <unistd.h>
#include <monitor.h>
#include <libgen.h> // 引入libgen库
#include <cpu/cpu.h>
#include <inst.h>
#include <verilator.h>


#include <execinfo.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define BT_BUF_SIZE 100

void print_backtrace(void) {
    void *buffer[BT_BUF_SIZE];
    char **strings;
    
    int nptrs = backtrace(buffer, BT_BUF_SIZE);
    printf("Backtrace (%d frames):\n", nptrs);
    
    strings = backtrace_symbols(buffer, nptrs);
    if (strings == NULL) {
        perror("backtrace_symbols");
        return;
    }
    
    for (int j = 0; j < nptrs; j++)
        printf("%s\n", strings[j]);
    
    free(strings);
}

void signal_handler(int sig) {
    fprintf(stderr, "\n=== Received signal %d (%s) ===\n", sig, strsignal(sig));
    print_backtrace();
    
    // 恢复默认处理并重新引发信号
    signal(sig, SIG_DFL);
    raise(sig);
}

void setup_signal_handlers(void) {
    signal(SIGSEGV, signal_handler);  // 段错误
    signal(SIGABRT, signal_handler);  // 中止信号
    signal(SIGILL, signal_handler);   // 非法指令
    signal(SIGFPE, signal_handler);   // 浮点异常
    signal(SIGBUS, signal_handler);   // 总线错误
}


extern  char img[131072];

bool stop = false;
uint64_t ret_val = 0;
extern uint32_t inst_num;

void engine_start();
int is_exit_status_bad();
void stop_sim();



int main(int argc, char **argv)
{
	setup_signal_handlers();
	Verilated::commandArgs(argc, argv);
	init_monitor(argc, argv);
	engine_start();
	stop_sim();
	return is_exit_status_bad();
}
