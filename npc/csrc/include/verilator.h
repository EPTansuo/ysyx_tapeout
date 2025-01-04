#ifndef _VERILATOR_H_
#define _VERILATOR_H_

#include <autoconf.h>
#include <verilated.h>
#include <common.h>

/*
#define MODULES(_) \
_(ysyx_npc) _(ysyx_23060246) _(WBU) _(LSU) _(IDU) _(IFU) \
_(ICache) _(CPU) _(Regfile) _(EXU) _(CSR) MUXDEF(CONFIG_RVE, _(regs_16x32), _(regs32x32)) \
_(_Dpi) IFDEF(CONFIG_USE_SOC, _(ysyxSoCFull)) IFNDEF(CONFIG_USE_SOC, _(ysyxSoCASIC)) 


#ifdef CONFIG_USE_SOC
    #define INCLUDE_MODULE(module) #include "VysyxSoCFull_" #module ".h"
#else
    #define INCLUDE_MODULE(module) #include "Vysyx_23060246_" #module ".h"
#endif

*/

#ifdef CONFIG_USE_SOC
#include "VysyxSoCFull.h"
#include "VysyxSoCFull__Dpi.h"
#include "VysyxSoCFull_CSR.h"
#include "VysyxSoCFull_EXU.h"
#include "VysyxSoCFull_Regfile.h"
#ifdef CONFIG_RVE
#include "VysyxSoCFull_regs_16x32.h"
#else  
#include "VysyxSoCFull_regs_32x32.h"
#endif 
#include "VysyxSoCFull_ysyx_23060246.h"
#include "VysyxSoCFull_ysyx_npc.h"
#include "VysyxSoCFull_ysyxSoCFull.h"
#include "VysyxSoCFull_ysyxSoCASIC.h"
#include "VysyxSoCFull_WBU.h"
#include "VysyxSoCFull_LSU.h"
#include "VysyxSoCFull_IDU.h"
#include "VysyxSoCFull_IFU.h"
#include "VysyxSoCFull_CPU.h"
#include "VysyxSoCFull_ICache.h"
#else
#include "Vysyx_23060246.h"
#include "Vysyx_23060246__Dpi.h"
#include "Vysyx_23060246_CSR.h"
#include "Vysyx_23060246_EXU.h"
#include "Vysyx_23060246_Regfile.h"
#ifdef CONFIG_RVE
#include "Vysyx_23060246_regs_16x32.h"
#else 
#include "Vysyx_23060246_regs_32x32.h"
#endif 
#include "Vysyx_23060246_ysyx_23060246.h"
#include "Vysyx_23060246_ysyx_npc.h"
#include "Vysyx_23060246_WBU.h"
#include "Vysyx_23060246_LSU.h"
#include "Vysyx_23060246_IDU.h"
#include "Vysyx_23060246_IFU.h"
#include "Vysyx_23060246_ICache.h"
//#include "Vysyx_23060246_CPU.h"
#endif 

#ifdef CONFIG_WAVE_VCD
#include <verilated_vcd_c.h>
#endif 
#ifdef CONFIG_WAVE_FST 
#include <verilated_fst_c.h>
#endif 

#ifdef CONFIG_USE_NVBOARD
#include <nvboard.h>
#endif 

#define VTOP_NAME MUXDEF(CONFIG_USE_SOC, VysyxSoCFull, Vysyx_23060246)


extern VTOP_NAME* top ;

#define NPC_CPU MUXDEF(CONFIG_USE_SOC, (top->ysyxSoCFull->asic->cpu->cpu->cpu_npc), \
                        (top->ysyx_23060246->cpu_npc))

#define REGS (NPC_CPU->regfile->regs_ext->Memory)
#define PC (NPC_CPU->wbu->io_in_bits_npc)
#define NPC (NPC_CPU->wbu->io_in_bits_pc)
#define CSR (NPC_CPU->exu->csr)
#define WBU_VALID (NPC_CPU->wbu->wbu_valid)
// #define INST (NPC_CPU->ifu->io_out_bits_inst)
#define INST (NPC_CPU->wbu->io_in_bits_inst)
#endif // !_VERILATOR_H_


