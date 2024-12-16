#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>

void _trm_init();


#define ALIGNED   // 段对齐
 #define ONE_STAGE_BL 


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
    *((uintptr_t *)dst) = *((uintptr_t *)src); 
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
  char *p = &_bss_start;
//   while(dst < & _data_end) {
//     *dst++ = *src++;
//   }
//   while(p < &_bss_end) {
//     *p++ = 0;
//   }
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
  _trm_init();
}

void __attribute__((section(".fsbl"))) _fsbl_init(){
  bootloader();
}
#endif 