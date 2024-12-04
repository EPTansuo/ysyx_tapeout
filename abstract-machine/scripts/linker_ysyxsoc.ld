ENTRY(_start)

PHDRS { text PT_LOAD; data PT_LOAD; }

MEMORY {
  mrom : ORIGIN = 0x20000000, LENGTH = 4K
  sram : ORIGIN = 0x0f000000, LENGTH = 8K
}

SECTIONS {

  . = ORIGIN(mrom);
  .text : {
    *(entry)
    *(.text*)
    _text_end = .;
  } > mrom AT> mrom : text

  etext = .;
  _etext = .;

  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > mrom AT>mrom

  _ram_data_start = .;

  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*)
    _data_end = .;
  } > sram AT>mrom : data

  edata = .;
  _data = .;

  .bss : {
    _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
    _bss_end = .;
  } > sram AT> mrom

  _heap_start = ALIGN(0x10);
  /*. =  _heap_start + 0x500;*/
  . = ORIGIN(sram) + 0x1000;
  _heap_end = .;
  _stack_end = .;
  /*_stack_top = 0x0f001200;*/
  . = ORIGIN(sram) + 0x2000;
  _stack_pointer = .;
  end = .;
  _end = .;
}
