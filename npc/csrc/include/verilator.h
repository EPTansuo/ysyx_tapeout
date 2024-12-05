#ifndef _VERILATOR_H_
#define _VERILATOR_H_

#include <verilated_vcd_c.h>
#include <verilated.h>


#include "VysyxSoCFull.h"
#include "VysyxSoCFull__Dpi.h"
#include "VysyxSoCFull_CSR.h"
#include "VysyxSoCFull_EXU.h"
#include "VysyxSoCFull_Regfile.h"
#include "VysyxSoCFull_regs_32x32.h"
#include "VysyxSoCFull_ysyx_23060246.h"
#include "VysyxSoCFull_ysyx_npc.h"
#include "VysyxSoCFull_ysyxSoCFull.h"
#include "VysyxSoCFull_ysyxSoCASIC.h"
#include "VysyxSoCFull_WBU.h"
#include "VysyxSoCFull_IFU.h"
#include "VysyxSoCFull_CPU.h"


extern VysyxSoCFull* top ;

#define NPC_CPU (top->ysyxSoCFull->asic->cpu->cpu->cpu_npc)

#define REGS (NPC_CPU->regfile->regs_ext->Memory)
#define PC (NPC_CPU->ifu->pc)
#define CSR (NPC_CPU->exu->csr)
#define WBU_VALID (NPC_CPU->wbu->wbu_valid)

#endif // !_VERILATOR_H_


