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

#define UART_BASE 0x10000000L
#define UART_TX   0



void putch(char ch) {
  io_write(AM_UART_TX, ch);
}

#define ALIGNED   // 段对齐
#undef ONE_STAGE_BL


#ifndef ONE_STAGE_BL

extern char _text_start, _data_start, _data_end, _text_src, _bss_start, _bss_end;

void __attribute__((section(".bootloader"))) bootloader(){
  char *src = &_text_src;
  char *dst = &_text_start;
    
  char *p = &_bss_start;


#ifdef ALIGNED
  while (dst <= &_data_end) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  
    dst += 4;
    src += 4;
  }
  while (p <= &_bss_end) {
    *((uintptr_t *)p) = 0; 
    p += 4;
  }
#else
  // 处理开头的不对齐
  while ((uintptr_t)dst % 4 != 0 && dst < &_data_end) {
    *dst++ = *src++; 
  }
  while (dst + 4 <= &_data_end) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  
    dst += 4;
    src += 4;
  }
  while (dst + 4 <= &_data_end) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  // 处理最后的不对齐
    dst += 4;
    src += 4;
  }
  while ((uintptr_t)p % 4 != 0 && p < &_bss_end) {
    *p++ = 0;  
  }
  while (p + 4 <= &_bss_end) {
    *((uintptr_t *)p) = 0; 
    p += 4;
  }
  while (p + 4 <= &_bss_end) {
    *((uintptr_t *)p) = 0; 
    p += 4;
  }
#endif
  _trm_init();
}

extern char _bootloader_src, _siflash_ssbl, _eiflash_ssbl;

void __attribute__((section(".fsbl"))) _fsbl_init(){
  // ioe_init();
  // printf("_text_start:%x\n", &_text_start);
  // printf("_data_end:%x\n", &_data_end);
  char *src = &_bootloader_src;   // LMA, flash
  char *dst = &_siflash_ssbl;     // VMA, psram

#ifdef ALIGNED
  while (dst <= &_eiflash_ssbl) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  
    dst += 4;
    src += 4;
  }
#else
  while ((uintptr_t)dst % 4 != 0 && dst < &_eiflash_ssbl) {
    *dst++ = *src++; 
  }
  while (dst + 4 <= &_eiflash_ssbl) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  
    dst += 4;
    src += 4;
  }
  while (dst + 4 <= &_eiflash_ssbl) {
    *((uintptr_t *)dst) = *((uintptr_t *)src);  // 处理最后的不对齐
    dst += 4;
    src += 4;
  }
#endif
  bootloader();
}

#endif // !ONE_STAGE_BL


#ifdef ONE_STAGE_BL
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
  _trm_init();
}

void __attribute__((section(".fsbl"))) _fsbl_init(){

  bootloader();
}
#endif 

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
  // char line1[] = {"mvendorid: 0x"};
  // char line2[] = {"marchid: "};
   printf("mvendorid: 0x%x\n", mvendorid);
   printf("marchid: %d\n", marchid);
  // putstr(line1);
  // putstr(line2);
}



void halt(int code) {
  asm volatile("ebreak");
  while (1);
}

void UART_init();
void _trm_init() {
  ioe_init();
  // print_stuID();
  int ret = main(mainargs);
  halt(ret);
}
