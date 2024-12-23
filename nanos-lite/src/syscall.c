#include <common.h>
#include "syscall.h"
#include <sys/time.h>
void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;

  switch (a[0]) {
    case SYS_yield: {
      c->GPRx = 0; yield(); 
      break;
    }
    case SYS_time: {
      uint64_t time = io_read(AM_TIMER_UPTIME).us;
      ((struct timeval *)a[1])->tv_sec = time / 1000000;
      ((struct timeval *)a[1])->tv_usec = time % 1000000;
      c->GPRx = 0;
      break;
    }
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
