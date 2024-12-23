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


size_t sys_write(int fd, const void *buf, size_t len);
time_t sys_time(struct timeval *t){ 
  time_t time = io_read(AM_TIMER_UPTIME).us;
  t->tv_sec  = time/1000000;
  t->tv_usec = time%1000000;
  return time;
};

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;
#ifdef STRACE
Log("SYSCALL(%s, %d, %d, %d)", syscalls[a[0]], a[1], a[2], a[3]);
#endif 


  switch (a[0]) {
    case SYS_exit: {
      halt(0);

      break;
    }
    case SYS_yield: {
      yield(); c->GPRx = 0; 
      break;
    }
    case SYS_time: {
      c->GPRx = sys_time((struct timeval *)a[1]);
      break;
    }
    case SYS_brk: {
      
    }
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
