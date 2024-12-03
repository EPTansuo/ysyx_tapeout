ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }

SECTIONS {

  . = _mrom_start;  /* 程序加载地址 */

  /* 代码段，放置在 MROM 中 */
  .text : {
    *(entry)
    *(.text*)   /* 所有代码段 */
  } : text

  etext = .;
  _etext = .;

  /* 常量数据段，放置在 MROM 中 */
  .rodata : {
    *(.rodata*)
  }

  /* 数据段，放置在 MROM 中 */
  .data : {
    *(.data*)
  } : data

  edata = .;
  _data = .;

  /* BSS 段，放置未初始化的全局变量 */
  .bss : {
    _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  }

  _stack_top = ALIGN(0x1000);  /* 栈区的起始位置，4KB对齐 */

  /* 栈空间分配在 SRAM 中，起始地址为 0x0f000000 */
  . = _sram_start + 0x8000;  /* 分配8KB的空间，栈从这个地址开始 */
  _stack_pointer = .;        /* 堆栈指针的地址 */

  /* 设置堆区的起始地址，堆区位于栈下方 */
  . = _stack_pointer - 0x1000; /* 堆占用 4KB */
  _heap_start = .;

  end = .;
  _end = .;
}
