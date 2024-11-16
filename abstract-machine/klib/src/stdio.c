#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>
#include <string.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

static inline int _pow(int base, int exp)
{
  int ret = 1;
  for(int i=0; i<exp; i++)
  {
      ret *= base;
  }
  return ret;
}

int _print_itoa(int num){
  int digits = 0;
  int bits = 0;
  int long _num = num;
  if (_num == 0) {
    putch('0');
    bits = 1;
  }
  else {
    if (_num < 0) {
      putch('-');
      _num = -_num;
    }
    int long temp = _num;
    while (temp != 0) {   //判断有几位数
      temp /= 10;
      bits++;
      digits++;
    }
    temp = _num;
    while (bits--) {
      putch('0' + (temp/_pow(10,bits) % 10));
    }
  }
  return digits;

}



int vprintf( const char *fmt, va_list ap) {
  int len = 0;

  while (*fmt) {
    if (*fmt == '%') {
      fmt++; // 跳过 '%'

      while(*fmt >= '0' && *fmt <= '9'){ //直接忽略类似于%02d中的02
        fmt++;
      }

      if (*fmt == 'd') {
        int num = va_arg(ap, int);
        len += _print_itoa(num);

      } else if (*fmt == 's') {
        const char *str = va_arg(ap, const char *);
        int str_len = strlen(str);
        for (int i = 0; i < str_len; i++) {
          putch(str[i]);
          len++;
        }
      } else if(*fmt == 'c') {
          char ch = (char)va_arg(ap, int);
          putch(ch);
          len++;
      }
    } else {
      putch(*fmt);
      len++;
    }
    fmt++;
  }

  return len;
}




int printf(const char *fmt, ...) {
  //panic("Not implemented");
  va_list args;
  va_start(args, fmt);
  int ret = vprintf( fmt, args);
  va_end(args);
  return ret;
}




int _itoa(char* dst, int num){
  int digits = 0;
  int bits = 0;
  int long _num = num;
  if (_num == 0) {
    dst[digits++] = '0';
        bits = 1;
  } 
  else {
    if (_num < 0) {
      dst[digits++] = '-';
      _num = -_num;
    }
    int temp = _num;
    while (temp != 0) {   //判断有几位数
      temp /= 10;
      bits++;
          digits++;
    }
    temp = _num;
    while (temp != 0) {   
      dst[--digits] = '0' + (temp % 10);
      temp /= 10;
    }
  }
  dst[digits+bits] = '\0';
  //printf("iota:dst:%s\n",dst);
  return digits+bits;
}

int itoa_(char* dst, int num){
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
        int digits = _itoa(buf, num);
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
