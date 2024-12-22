#include "verilator.h"
#include <locale.h>
#include <color.h>
#include <debug.h>

#define IFU (NPC_CPU->ifu)
#define IDU (NPC_CPU->idu)
#define EXU (NPC_CPU->exu)
#define LSU (NPC_CPU->lsu)
#define WBU (NPC_CPU->wbu)
#define ICACHE (NPC_CPU->icache)

/************** IFU ***********/
static uint32_t ifu_cnt = 0;

/************** IDU ***********/
static uint32_t inst_compute_cnt = 0;
static uint32_t inst_branch_cnt = 0;
static uint32_t inst_jump_cnt = 0;
static uint32_t inst_ldst_cnt = 0;
static uint32_t inst_csr_cnt = 0;
static uint64_t cycle_compute_cnt = 0;
static uint64_t cycle_branch_cnt = 0;
static uint64_t cycle_jump_cnt = 0;
static uint64_t cycle_ldst_cnt = 0;
static uint64_t cycle_csr_cnt = 0;

/************** EXU ***********/
static uint32_t alu_arith_cnt = 0;
static uint32_t alu_logic_cnt = 0;
static uint32_t alu_shift_cnt = 0;
static uint32_t alu_cmp_cnt = 0;
static uint32_t alu_copy_cnt = 0;

/************** LSU ************/
static uint32_t load_cnt = 0;
static uint32_t store_cnt = 0;
static uint64_t cycle_load_cnt = 0;
static uint64_t cycle_store_cnt = 0;

/************** WBU ************/
static uint64_t inst_cnt = 0;
static uint64_t cycle_cnt = 0;

/************ ICache ************/
static uint64_t icache_access_cnt = 0;
static uint64_t icache_hit_cnt = 0;

void perf_get_data(){
    ifu_cnt = IFU->ifu_cnt;

    inst_compute_cnt = IDU->inst_compute_cnt;
    inst_branch_cnt = IDU->inst_branch_cnt;
    inst_jump_cnt = IDU->inst_jump_cnt;
    inst_ldst_cnt = IDU->inst_ldst_cnt;
    inst_csr_cnt = IDU->inst_csr_cnt;
    cycle_compute_cnt = IDU->cycle_compute_cnt;
    cycle_branch_cnt = IDU->cycle_branch_cnt;
    cycle_jump_cnt = IDU->cycle_jump_cnt;
    cycle_ldst_cnt = IDU->cycle_ldst_cnt;
    cycle_csr_cnt = IDU->cycle_csr_cnt;

    alu_arith_cnt = EXU->alu_arith_cnt;
    alu_logic_cnt = EXU->alu_logic_cnt;
    alu_shift_cnt = EXU->alu_shift_cnt;
    alu_cmp_cnt = EXU->alu_cmp_cnt;
    alu_copy_cnt = EXU->alu_copy_cnt;

    load_cnt = LSU->load_cnt;
    store_cnt = LSU->store_cnt;
    cycle_load_cnt = LSU->cycle_load_cnt;
    cycle_store_cnt = LSU->cycle_store_cnt;

    inst_cnt = WBU->inst_cnt;
    cycle_cnt = WBU->cycle_cnt;

    icache_access_cnt = ICACHE->icache_access_cnt;
    icache_hit_cnt = ICACHE->icache_hit_cnt;
}

FILE *fp;

#define PRINT_PERF(name, cnt, process_cnt, unit) \
    printf("%24s: %'14lu         # %10.4lf %s\n", \
           name, (uint64_t)cnt, process_cnt, unit);\
    fprintf(fp, "%24s: %'14lu         # %10.4lf %s\n", \
           name, (uint64_t)cnt, process_cnt, unit);

void perf_statistic(){
    perf_get_data();
    char buf[100];
    sprintf(buf, "%s/build/perf_statistic.txt", getenv("NPC_HOME"));
    fp = fopen(buf,"w");
    fprintf(fp, "\n================================= PERF STATISTIC =================================\n");
    printf("\n================================= PERF STATISTIC =================================\n");
    setlocale(LC_NUMERIC, "en_US.UTF-8");
    
    PRINT_PERF("Total Cycles", cycle_cnt, 0.0, "");
    printf(L_BLUE);
    PRINT_PERF("Total Insts", inst_cnt, (double)inst_cnt/cycle_cnt, "IPC");
    printf(COLOR_NONE);
    printf("\n----------------------------------- Inst Fetch -----------------------------------\n");
    PRINT_PERF("Inst Fetch CNT", ifu_cnt, (double)ifu_cnt/inst_cnt*100, "% of inst");
    printf("\n----------------------------------- Inst Type ------------------------------------\n");
    PRINT_PERF("Compute Insts CNT", inst_compute_cnt, (double)inst_compute_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Branch  Insts CNT", inst_branch_cnt, (double)inst_branch_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Jump    Insts CNT", inst_jump_cnt, (double)inst_jump_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("ld/st   Insts CNT", inst_ldst_cnt, (double)inst_ldst_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("CSR     Insts CNT", inst_csr_cnt, (double)inst_csr_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Compute Cyeles CNT", cycle_compute_cnt, (double)cycle_compute_cnt/inst_compute_cnt, "cycle per inst");
    PRINT_PERF("Branch  Cyeles CNT", cycle_branch_cnt, (double)cycle_branch_cnt/inst_branch_cnt, "cycle per inst");
    PRINT_PERF("Jump    Cyeles CNT", cycle_jump_cnt, (double)cycle_jump_cnt/inst_jump_cnt, "cycle per inst");
    PRINT_PERF("ld/st   Cyeles CNT", cycle_ldst_cnt, (double)cycle_ldst_cnt/inst_ldst_cnt, "cycle per inst");
    PRINT_PERF("CSR     Cyeles CNT", cycle_csr_cnt, (double)cycle_csr_cnt/inst_csr_cnt, "cycle per inst");

    printf("\n----------------------------------- ALUOP Type ------------------------------------\n");
    PRINT_PERF("Alu Arith CNT", alu_arith_cnt, (double)alu_arith_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Alu Logic CNT", alu_logic_cnt, (double)alu_logic_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Alu Shift CNT", alu_shift_cnt, (double)alu_shift_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Alu Cmp   CNT", alu_cmp_cnt, (double)alu_cmp_cnt/inst_cnt*100, "% of inst");
    PRINT_PERF("Alu Copy  CNT", alu_copy_cnt, (double)alu_copy_cnt/inst_cnt*100, "% of inst");

    printf("\n---------------------------------- Load / Store -----------------------------------\n");
    PRINT_PERF("Load CNT", load_cnt, (double)load_cnt/inst_ldst_cnt*100, "% of ld/st inst");
    PRINT_PERF("Store CNT", store_cnt, (double)store_cnt/inst_ldst_cnt*100, "% of ld/st inst");
    PRINT_PERF("Load Cycles CNT", cycle_load_cnt, (double)cycle_load_cnt/load_cnt, "cycle per load");
    PRINT_PERF("Store Cycles CNT", cycle_store_cnt, (double)cycle_store_cnt/store_cnt, "cycle per store");

    printf("\n-------------------------------------- ICache --------------------------------------\n");
    PRINT_PERF("ICache Access CNT", icache_access_cnt, 0.0, "");
    PRINT_PERF("ICache HIT CNT", icache_hit_cnt, (double)icache_hit_cnt/icache_access_cnt*100, "% Hit Rate");
    
    fclose(fp);
}