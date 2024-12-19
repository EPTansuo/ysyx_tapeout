#include <utils/utils.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <time.h>

#define COMPRESSION_LEVEL  Z_DEFAULT_COMPRESSION


gzFile log_open(const char *filename) {
    return gzopen(filename, "wb");
}

int log_printf(gzFile log_file, char *format, ...) {
    static char buffer[512];
    va_list args;
    va_start(args, format);
    int len = vsnprintf(buffer, sizeof(buffer), format, args);
    va_end(args);

    if (len < 0) {
        return -1;
    }

    if (gzwrite(log_file, buffer, len) != len) {
        return -1;
    }
    return 0;
}

int log_write_bin(gzFile log_file, char*buf, size_t len){
    if(gzwrite(log_file, buf, len) != len) {return -1;}
    return 0;
}

void log_close(gzFile log_file) {
    if (log_file) {
        gzclose(log_file);
        log_file = NULL;
    }
}

