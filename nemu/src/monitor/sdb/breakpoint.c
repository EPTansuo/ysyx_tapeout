#include <breakpoint.h>

#define BP_POOL_SIZE 32

typedef struct {
  int NO;
  bool valid;
  vaddr_t addr;
} BP; 

BP bp_pool[BP_POOL_SIZE];


void init_bp_pool() {
  for (int i = 0; i < BP_POOL_SIZE; i++) {
    bp_pool[i].NO = i;
    bp_pool[i].valid = false;
  }
}


void add_breakpoint(vaddr_t addr) {
  for (int i = 0; i < BP_POOL_SIZE; i++) {
    if (!bp_pool[i].valid) {
      bp_pool[i].valid = true;
      bp_pool[i].addr = addr;
      printf("Breakpoint %d: 0x%08x\n", i, addr);
      return;
    }
  }
  printf("No more space for breakpoint\n");
}

void del_breakpoint(vaddr_t addr ) {
  for (int i = 0; i < BP_POOL_SIZE; i++) {
    if (bp_pool[i].valid && bp_pool[i].addr == addr) {
      bp_pool[i].valid = false;
      printf("Delete breakpoint %d: 0x%08x\n", i, addr);
      return;
    }
  }
  printf("No such breakpoint\n");
}

void scan_breakpoint(vaddr_t addr)
{
  for (int i = 0; i < BP_POOL_SIZE; i++)
  {
    if (bp_pool[i].valid && bp_pool[i].addr == addr)
    {
      printf("Breakpoint %d: 0x%08x\n", i, addr);
      nemu_state.state = NEMU_STOP;
      return;
    }
  }
}