#include <stdio.h>
#include <sys/time.h>
#include <unistd.h>

int main() {
    struct timeval tv, last_tv;
    long seconds, useconds;
    int interval = 500000;  // 0.5 秒，以微秒为单位

    gettimeofday(&last_tv, NULL);

    while (1) {
        gettimeofday(&tv, NULL);
        seconds  = tv.tv_sec  - last_tv.tv_sec;
        useconds = tv.tv_usec - last_tv.tv_usec;
        if (useconds < 0) {
            useconds += 1000000;
            seconds--;
        }
        if ((seconds * 1000000 + useconds) >= interval) {
            printf("hello\n");
            last_tv = tv;
        }

    }

    return 0;
}
