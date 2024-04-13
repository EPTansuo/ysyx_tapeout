#include "./include/ftrace.h"
#include <stdio.h>

bool ftrace_enabled = false;

void ftrace_enable(bool enable)
{
        printf("%s: %d\n",__func__,ftrace_enabled);
        ftrace_enabled = enable;
        printf("%s: %d\n",__func__,ftrace_enabled);
}

