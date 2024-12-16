#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>
#include <stdio.h>

extern char _heap_start;

int main(const char *args);
void _trm_init();


extern char _pmem_start;

#define PMEM_SIZE (8 * 1024 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)

Area heap = RANGE(&_heap_start, PMEM_END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;


void putch(char ch) {
  io_write(AM_UART_TX, ch);
}


void print_stuID(){
  uint32_t mvendorid, marchid;
  asm volatile (
        "csrr %0, 0xF11\n"
        : "=r" (mvendorid)
    );
  asm volatile (
        "csrr %0, 0xF12\n"
        : "=r" (marchid)
    );
   printf("mvendorid: 0x%x\n", mvendorid);
   printf("marchid: %d\n", marchid);
}



void halt(int code) {
  asm volatile("ebreak");
  while (1);
}

void _trm_init() {
  ioe_init();
  // print_stuID();
  int ret = main(mainargs);
  halt(ret);
}
