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

  _stack_top = ALIGN(0x10);

  /* 栈空间分配在 SRAM 中，起始地址为 0x0f000000 */
  . = ORIGIN(sram) + 0x2000;  /* 分配8KB的空间，栈从这个地址开始 */
  _stack_pointer = .;        /* 堆栈指针的地址 */

  /* 设置堆区的起始地址，堆区位于栈下方 */
  _heap_start = .;

  end = .;
  _end = .;
}
