#include <cpu/cpu.h>
#include <isa.h>
#include <Vcpu.h>
#include <Vcpu_cpu.h>
#include <Vcpu_pc.h>
#include <fmt-def.h>
#include <Vcpu_ifu.h>
#include <Vcpu_inst_rom.h>
#include <inst.h>
#include <watchpoint.h>
#include <cpu/difftest.h>

#define MAX_INST_TO_PRINT 10001

CPU_state cpu = {};

static bool g_print_step = false;

extern Vcpu* top;
extern VerilatedVcdC * tfp;
extern VerilatedContext* contextp;

void disassemble(char* logbuf, size_t logbuf_size, word_t pc);


static void trace_and_difftest(){
 // IFDEF(CONFIG_DIFFTEST, difftest_step(top->cpu->pc1->pc, top->cpu->pc1->pc + top->cpu->pc1->pc_offset));
  scan_watchpoint();
}

void cpu_eval_dump(){
  top->eval();
  if (tfp != NULL)
  {
    tfp->dump(contextp->time());
    contextp->timeInc(1);
  }
}

void cpu_single_cycle(){
	int i = 2;
	while((i--))
	{
		top->clk = !top->clk;
		cpu_eval_dump();
	}
}

void cpu_reset(int n){
	top->rst = RESET_ENABLE;
	while(n--)cpu_single_cycle();	
	top->rst = RESET_DISABLE;
}


// void init_cpu_exec(Vcpu* _top, VerilatedVcdC* _tfp, VerilatedContext* _contextp){
//         top = _top;
//         tfp = _tfp;
//         contextp = _contextp;
//         cpu_reset(3);
// }


void assert_fail_msg() {
  isa_reg_display();
 // statistic();
}

static void exec_once(){
  char logbuf[50];
  cpu_single_cycle();
  disassemble(logbuf,50,top->cpu->pc1->pc);
  print_inst(top->cpu->pc1->pc);
  printf("\t%s\n", logbuf);
  
}


static void execute(uint64_t n) {
  for (;n > 0; n --) {
    exec_once();
    
    trace_and_difftest();
    if (npc_state.state != NPC_RUNNING) break;
    IFDEF(CONFIG_DEVICE, device_update());
  }
}



void cpu_exec(uint64_t n) {
  g_print_step = (n < MAX_INST_TO_PRINT);
  switch (npc_state.state) {
    case NPC_END: case NPC_ABORT:
      printf("Program execution has ended. To restart the program, exit NPC Simulation and run again.\n");
      return;
    default: npc_state.state = NPC_RUNNING;
  }


  execute(n);


  switch (npc_state.state) {
    case NPC_RUNNING: npc_state.state = NPC_STOP; break;

    case NPC_END: case NPC_ABORT:
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) :
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
#ifdef CONFIG_ITRACE
        if(npc_state.state == NPC_END && npc_state.halt_ret != 0) print_iringbuf();
#endif // CONFIG_ITRACE
    
      // fall through
    case NPC_QUIT: ;
  }
}


