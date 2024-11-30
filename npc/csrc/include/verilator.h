#ifndef _VERILATOR_H_
#define _VERILATOR_H_

#include <verilated_vcd_c.h>
#include <verilated.h>

#include "Vnpc.h"
#include "Vnpc_npc.h"
#include "Vnpc_ysyx_npc.h"
#include "Vnpc__Dpi.h"
#include "Vnpc_Regfile.h"
#include "Vnpc_CSR.h"
#include "Vnpc_WBU.h"

extern Vnpc* top;

#define REGS (top->npc->cpu->regfile->regs)
#define PC (top->npc->cpu->pc)
#define CSR (top->npc->cpu->csr)
#define WBU_READY (top->npc->cpu->wbu->io_out_ready)

#endif // !_VERILATOR_H_


