#include "../obj_dir/Vexample.h"
#include "verilated.h"
#include <iostream>
#include <verilated_vcd_c.h>
#include <nvboard.h>

static TOP_NAME dut;

void nvboard_bind_all_pins(TOP_NAME *top);

static void single_cycle()
{
	//dut.clk = 0;
	dut.eval();
	//dut.clk = 1;
	dut.eval();
}


void verilator_sim(int argc, char **argv)
{
	Verilated::commandArgs(argc, argv);
	Verilated::traceEverOn(true);
	Vexample *top = new Vexample;

	VerilatedVcdC *tfp = new VerilatedVcdC;

	top->trace(tfp, 0);
	tfp->open("wave.vcd");

	for (int i = 0; i < 10; i++)
	{
		top->a = i & 1;
		top->b = (i >> 1) & 1;

		top->eval();
		tfp->dump(i);

		std::cout << "a = " << (top->a ? "1" : "0")
			  << ", b = " << (top->b ? "1" : "0")
			  << ", f = " << (top->f ? "1" : "0") << std::endl;
	}

	top->final();
	tfp->close();

	delete top;
}

void nvboard_sim()
{
}

int main(int argc, char **argv)
{
	nvboard_bind_all_pins(&dut);
	nvboard_init();


	while (1)
	{
		nvboard_update();
		single_cycle();
	}
}
