ENTRY(_start)

PHDRS { text PT_LOAD; data PT_LOAD; fsbl PT_LOAD; bootloader PT_LOAD; rodata PT_LOAD; }

MEMORY {
  mrom(rx)   : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rwx)  : ORIGIN = 0x0f000000, LENGTH = 8K
  flash(rx)  : ORIGIN = 0x30000000, LENGTH = 16M
  psram(rwx) : ORIGIN = 0x80000000, LENGTH = 4M
  sdram(rwx) : ORIGIN = 0xa0000000, LENGTH = 64M
}

SECTIONS {

  . = ORIGIN(flash);

  .fsbl :{
  . = ALIGN(8);
   *(entry)
    *(.fsbl*)
  } > flash :fsbl

  _bootloader_src = LOADADDR(.bootloader);
  .bootloader :{
    _siflash_ssbl = .;
    *(.bootloader*)
    _eiflash_ssbl = .;
  } > psram AT> flash : bootloader


  _text_src = LOADADDR(.text);

  .text : {
    _text_start = .;
    *(.text*)
    _text_end = .;
  } > flash AT> flash : text




  etext = .;
  _etext = .;

  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > psram AT> flash : rodata



  _data_src = LOADADDR(.data);

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

  _stack_top = ORIGIN(sram);

  _stack_pointer = ORIGIN(sram) + LENGTH(sram);



  . = ORIGIN(psram);
  _heap_start = .;
  . = ORIGIN(psram) + LENGTH(psram);
  _heap_end = .;

/*   end = .; */
/*   _end = .; */
}
