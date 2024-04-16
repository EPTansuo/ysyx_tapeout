#include <unistd.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <monitor.h>
#include <stdbool.h>
#include <stdint.h>
#include <inst.h>
#include <utils/utils.h>
#include <color.h>
#include <verilated.h>

char* img_file  = NULL;
bool verbose = false;
void init_sim();
void init_sdb();


static void welcome() {
  Log("Trace: %s", MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN), ANSI_FMT("OFF", ANSI_FG_RED)));
  IFDEF(CONFIG_TRACE, Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig"));
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-NPC!\n", ANSI_FG_YELLOW ANSI_BG_RED CONFIG_ISA NONE);
  printf("For help, type \"help\"\n");
  //Log("Exercise: Please remove me in the source code and compile NEMU again.");
  //assert(0);
}




const char *get_img_file(){
        return img_file;
}




static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"help"     , no_argument      , NULL, 'h'},
    {"verbose"  , no_argument      , NULL, 'v'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-hv", table, NULL)) != -1) {
    switch (o) {
      case 'v': verbose = true; break; 
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-v,--verbose           print detail infomation of regs\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}



void init_monitor(int argc, char** argv){
        Verilated::commandArgs(argc, argv);
        parse_args(argc, argv);
        init_sim();
        load_img();
        init_disasm();
        init_sim();
        init_sdb();
        welcome();
}

