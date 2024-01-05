#include "../obj_dir/Vexample.h"
#include "verilated.h" 
#include <iostream>
#include <verilated_vcd_c.h>


int main(int argc, char**argv)
{
	Verilated::commandArgs(argc, argv); 
	Verilated::traceEverOn(true);
	Vexample* top = new Vexample;      
	
	VerilatedVcdC *tfp = new VerilatedVcdC;
	
	top->trace(tfp,0);
	tfp->open("wave.vcd");

	for (int i = 0; i < 4; i++) {
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
