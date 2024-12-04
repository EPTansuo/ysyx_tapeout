ENTRY(_start)

PHDRS { text PT_LOAD; data PT_LOAD; }
/*PHDRS {
  text PT_LOAD FLAGS(RX);
  data PT_LOAD FLAGS(RW);
  bss PT_LOAD FLAGS(RW);
}*/

MEMORY {
  mrom : ORIGIN = 0x20000000, LENGTH = 4K
  sram : ORIGIN = 0x0f000000, LENGTH = 8K
}

SECTIONS {

  . = ORIGIN(mrom);

  /* 代码段，放置在 MROM 中 */
  .text : {
    *(entry)
    *(.text*)   /* 所有代码段 */
  } > mrom AT> mrom : text

  etext = .;
  _etext = .;

  /* 常量数据段，放置在 MROM 中 */
  .rodata : {
    *(.rodata*)
  } > mrom AT> mrom : text

  /* 数据段，放置在 MROM 中 */

  _ram_data_start = .;

  .data : {
   _data_start = .;
    *(.data*)
   _data_end = .;
  } > sram AT> mrom : data

  edata = .;

   _data = .;

  /* BSS 段，放置未初始化的全局变量 */
  .bss : {
    _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
    _bss_end = .;
  } > sram AT> sram /* :bss*/

  _heap_start = ALIGN(0x10);
  . =  _heap_start;
  _heap_end = .;
  . = ORIGIN(sram) + 0x1000;
  _stack_end = .;
  . = ORIGIN(sram) + 0x2000;
  _stack_pointer = .;
  end = .;
  _end = .;
}
