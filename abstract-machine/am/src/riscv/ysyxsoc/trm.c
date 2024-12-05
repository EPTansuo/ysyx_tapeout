#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>

extern char _heap_start;
int main(const char *args);

extern char _pmem_start;

#define PMEM_SIZE (8 * 1024 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)

Area heap = RANGE(&_heap_start, PMEM_END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

#define UART_BASE 0x10000000L
#define UART_TX   0

void putch(char ch) {
  *(volatile char *)(UART_BASE + UART_TX) = ch;
}


extern char _ram_data_start, _data_start, _data_end, _bss_start, _bss_end;

void bootloader(){
  char *src = &_ram_data_start;
  char *dst = &_data_start;
  while(dst < & _data_end) {
    *dst++ = *src++;
  }

  char *p = &_bss_start;
  while(p < &_bss_end) {
    *p++ = 0;
  }
}


void halt(int code) {
  asm volatile("ebreak");
  while (1);
}

void UART_init();
void _trm_init() {
  bootloader();
  //ioe_init();
  int ret = main(mainargs);
  halt(ret);
}
