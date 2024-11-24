#ifndef _VERILATOR_H_
#define _VERILATOR_H_

#include <verilated_vcd_c.h>
#include <verilated.h>

#include "Vnpc.h"
#include "Vnpc_npc.h"
#include "Vnpc_CPU.h"
#include "Vnpc__Dpi.h"
#include "Vnpc_Regfile.h"

extern Vnpc* top;

#define REGS (top->npc->cpu->regfile->regs)
#define PC (top->npc->cpu->pc)

#endif // !_VERILATOR_H_


