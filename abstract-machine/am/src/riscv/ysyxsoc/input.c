#include <am.h>

#define PS2_BASE 0x10011000
#define PS2_KEY (*(volatile uint8_t*)(PS2_BASE))

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  kbd->keydown = 0;
  kbd->keycode = AM_KEY_NONE;

  if (PS2_KEY != 0) {
    kbd->keydown = 1;
    kbd->keycode = PS2_KEY;
  }

}
