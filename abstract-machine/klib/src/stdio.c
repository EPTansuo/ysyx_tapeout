#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>
#include <string.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int itoa(char* dst, int num){
  int digits = 0;
  int bits = 0;
  if (num == 0) {
    dst[digits++] = '0';
        bits = 1;
  } 
  else {
    if (num < 0) {
      dst[digits++] = '-';
      num = -num;
    }
    int temp = num;
    while (temp != 0) {   //判断有几位数
      temp /= 10;
      bits++;
          digits++;
    }
    temp = num;   
    while (temp != 0) {   
      dst[--digits] = '0' + (temp % 10);
      temp /= 10;
    }
  }
  dst[digits+bits] = '\0';
  //printf("iota:dst:%s\n",dst);
  return digits+bits;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  int len = 0;
  char buf[256]; 

  while (*fmt) {
    if (*fmt == '%') {
      fmt++; // 跳过 '%'
      if (*fmt == 'd') {
        int num = va_arg(ap, int);
        int digits = itoa(buf, num);
        for (int i = 0; i < digits; i++) {
          *out++ = buf[i];
          len++;
        }
      } else if (*fmt == 's') {
        const char *str = va_arg(ap, const char *);
        int str_len = strlen(str);
        for (int i = 0; i < str_len; i++) {
          *out++ = str[i];
          len++;
        }
      }
    } else {
      *out++ = *fmt;
      len++;
    }
    fmt++;
  }

  *out = '\0'; 
  return len;
}

int sprintf(char *out, const char *fmt, ...) {
  va_list args;
  va_start(args, fmt);
  int ret = vsprintf(out, fmt, args);
  va_end(args);
  return ret;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
