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


extern  char img[131072];

bool stop = false;
uint64_t ret_val = 0;
extern uint32_t inst_num;

void engine_start();
int is_exit_status_bad();
void stop_sim();



int main(int argc, char **argv)
{
	Verilated::commandArgs(argc, argv);
	init_monitor(argc, argv);
	engine_start();
	stop_sim();
	return is_exit_status_bad();
}
