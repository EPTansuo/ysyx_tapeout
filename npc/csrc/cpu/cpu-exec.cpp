#include <cpu/cpu.h>
#include <isa.h>
#include <fmt-def.h>
#include <inst.h>
#include <watchpoint.h>
#include <cpu/difftest.h>
#include <reg.h>
#include <verilator.h>
#include <memory/paddr.h>

#define MAX_INST_TO_PRINT 10001
bool first = true;

CPU_state npc_cpu = {};
uint64_t g_nr_guest_inst = 0;
static bool g_print_step = false;

extern VerilatedVcdC * tfp;
extern VerilatedContext* contextp;

void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
void device_update();

static void trace_and_difftest(){
  //printf("pc=0x%x, dnpc=0x%x\n",PC, PC + (top->cpu->pc1->pc_offset_en?top->cpu->pc1->pc_offset:0));
  //IFDEF(CONFIG_DIFFTEST, difftest_step(PC, PC + top->cpu->pc1->pc_offset));
  IFDEF(CONFIG_DIFFTEST, difftest_step(0,0));
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
		top->clock = !top->clock;
		cpu_eval_dump();
	}
}

void cpu_single_inst(){
  do{
    cpu_single_cycle();
  }while(WBU_READY);
    
    
}

void cpu_reset(int n){
	top->reset = RESET_ENABLE;
	while(n--)cpu_single_cycle();	
	top->reset = RESET_DISABLE;
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
  
  //cpu_single_cycle();
  cpu_single_inst();

  for(int i=0; i<MUXDEF(CONFIG_RVE,16,32); i++){
    npc_cpu.gpr[i] = gpr(i);
  }
  npc_cpu.pc = PC;

  npc_cpu.csr.mepc = CSR->mepc;
  npc_cpu.csr.mcause = CSR->mcause;
  npc_cpu.csr.mstatus = CSR->mstatus;
  npc_cpu.csr.mtvec = CSR->mtvec;
  
#ifdef CONFIG_ITRACE
  char logbuf[64];
  if(g_print_step){
   disassemble(logbuf, 64, PC , guest_to_host(PC), 4);
   printf("0x" FMT_WORD_HEX_WIDTH ":    ", PC);
    
    for(int j = 3; j >= 0; j--){
      printf("%02x ", ((uint8_t*)guest_to_host(PC))[j]);
    }
  
   printf("%s\n", logbuf);
  }
#endif 
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


