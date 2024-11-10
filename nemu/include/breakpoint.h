#ifndef __BREAKPOINT_H_
#define __BREAKPOINT_H_
#include <common.h>
#include <utils.h>

void init_bp_pool();

void add_breakpoint(vaddr_t addr);
void del_breakpoint(vaddr_t addr);

void scan_breakpoint();

#endif // !__BREAKPOINT_H_
