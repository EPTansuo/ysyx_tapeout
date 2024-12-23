#include <common.h>
#include "syscall.h"
#include <sys/time.h>

#ifdef STRACE
const char *syscalls[] = {"SYS_exit", "SYS_yield", "SYS_open",
"SYS_read", "SYS_write", "SYS_kill", "SYS_getpid", "SYS_close", 
"SYS_lseek", "SYS_brk", "SYS_fstat", "SYS_time", "SYS_signal", 
"SYS_execve", "SYS_fork", "SYS_link", "SYS_unlink", "SYS_wait",
"SYS_times", "SYS_gettimeofday"};
#endif 

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;

#ifdef STRACE
Log("SYSCALL: %s", syscalls[a[0]]);
#endif 


  switch (a[0]) {
    case SYS_exit: {
      //halt(0);

      break;
    }
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
