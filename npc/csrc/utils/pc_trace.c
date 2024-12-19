#include <utils/utils.h>
#include <stdio.h>
#include <common.h>
#include <fmt-def.h>

IFDEF(CONFIG_PC_TRACE, static 
MUXDEF(CONFIG_PC_TRACE_COMPRESS, gzFile, FILE*) pc_trace_fp;)


void init_pc_trace(){
  char buf[100];
  sprintf(buf, "%s/build/pc_trace.txt", getenv("NPC_HOME"));
#ifdef CONFIG_PC_TRACE_COMPRESS
  pc_trace_fp = log_open(buf ".gz");
#else 
  pc_trace_fp = fopen(buf, "wb");
#endif 
}

void pc_trace(word_t pc){
#ifdef CONFIG_PC_TRACE_COMPRESS
  log_printf(pc_trace_fp, "%lx\n", pc); 
#else  
  fprintf(pc_trace_fp, FMT_WORD_HEX "\n", pc);
#endif
}
void pc_trace_close(){
#ifdef CONFIG_PC_TRACE_COMPRESS
  log_close(pc_trace_fp);
#else 
  fclose(pc_trace_fp);
#endif
}
