ENTRY(_start)

PHDRS { text PT_LOAD; data PT_LOAD; }

MEMORY {
  mrom(rx)   : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rwx)  : ORIGIN = 0x0f000000, LENGTH = 8K
  flash(rx)  : ORIGIN = 0x30000000, LENGTH = 16M
  psram(rwx) : ORIGIN = 0x80000000, LENGTH = 4M
  sdram(rwx) : ORIGIN = 0xa0000000, LENGTH = 64M
}

SECTIONS {

  . = ORIGIN(flash);
  .text : {
    *(entry)
    *(.text*)
    _text_end = .;
  } > flash AT> flash : text

  etext = .;
  _etext = .;

  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > flash AT>flash


/*  .fsdl : {
    *(.fsbl*)
  } > flash*/

  _ram_data_start = .;

  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*)
    _data_end = .;
  } > psram AT>flash : data

  edata = .;
  _data = .;

  .bss : {
    _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
    _bss_end = .;
  } > psram AT> flash

    .heap :
  ALIGN(0x1000)
  {
    _heap_start = .;
    _heap_end = ORIGIN(psram) + LENGTH(psram) / 2;
    . = _heap_end;
  } > psram

  .stack :
  ALIGN(0x8)
  {
    _stack_top = .;
    _stack_pointer = ORIGIN(psram) + LENGTH(psram);
    . = _stack_pointer;
  } > psram


  end = .;
  _end = .;
}
