ENTRY(_start)

PHDRS {
  text PT_LOAD FLAGS (rx);
  data PT_LOAD FLAGS (rw);
  fsbl PT_LOAD FLAGS (rx);
  bootloader PT_LOAD FLAGS (rx);
  rodata PT_LOAD FLAGS (r);
}

MEMORY {
  mrom(rx)   : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rwx)  : ORIGIN = 0x0f000000, LENGTH = 8K
  flash(rx)  : ORIGIN = 0x30000000, LENGTH = 16M
  psram(rwx) : ORIGIN = 0x80000000, LENGTH = 4M
  sdram(rwx) : ORIGIN = 0xa0000000, LENGTH = 64M
}

SECTIONS {

  /* Start in flash */
  . = ORIGIN(flash);

  /* FSBL Section */
  .fsbl : {
    *(entry)
    *(.fsbl*)
  } > flash : fsbl

  /* Bootloader Section */
  _bootloader_src = LOADADDR(.bootloader);
  .bootloader : {
    _siflash_ssbl = .;
    *(.bootloader*)
    _eiflash_ssbl = .;
  } > sdram AT> flash : bootloader

  /* Text Section */
  _text_src = LOADADDR(.text);
  .text : {
    _text_start = .;
    *(.text*)
    _text_end = .;
  } > sdram AT> flash : text

  /* Read-only Data Section */
  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > sdram AT> flash : rodata

  /* Data Extra Section */
  .data.extra : {
    /* Section information for finsh shell */
    __fsymtab_start = .;
    KEEP(*(FSymTab))
    __fsymtab_end = .;
    . = ALIGN(8);
    __vsymtab_start = .;
    KEEP(*(VSymTab))
    __vsymtab_end = .;
    . = ALIGN(8);

    /* Section information for initial */
    . = ALIGN(8);
    __rt_init_start = .;
    KEEP(*(SORT(.rti_fn*)))
    __rt_init_end = .;
    . = ALIGN(8);

    __rt_utest_tc_tab_start = .;
    KEEP(*(UtestTcTab))
    __rt_utest_tc_tab_end = .;

    . = ALIGN(8);
    __am_apps_data_start = .;
    *(__am_apps.data*)
    *(__am_apps.sdata*)
    __am_apps_data_end = .;
    . = ALIGN(8);
  } > sdram : data_sdram  /* 正确分配到 sdram 的 data_sdram 程序头段 */

  /* Data Section */
  _data_src = LOADADDR(.data);
  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*)
    _data_end = .;
  } > sdram AT> flash : data_sdram  /* 正确分配到 sdram 的 data_sdram 程序头段 */

  edata = .;
  _data = .;

  /* BSS Extra Section */
  .bss.extra : {
    __am_apps_bss_start = .;
    *(__am_apps.bss*)
    *(__am_apps.sbss*)
    *(__am_apps.scommon*)
    __am_apps_bss_end = .;
  } > sdram : data_sdram  /* 正确分配到 sdram 的 data_sdram 程序头段 */

  /* BSS Section */
  .bss : {
    _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
    _bss_end = .;
  } > sdram AT> flash : data_sdram  /* 正确分配到 sdram 的 data_sdram 程序头段 */

  /* Stack */
  _stack_top = ORIGIN(sram);
  _stack_pointer = ORIGIN(sram) + LENGTH(sram);

  /* Heap */
  . = ORIGIN(psram);
  _heap_start = .;
  . = ORIGIN(psram) + LENGTH(psram);
  _heap_end = .;
}
