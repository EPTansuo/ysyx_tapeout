#include <unistd.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <monitor.h>
#include <stdbool.h>
#include <stdint.h>

static char* img_file  = NULL;
bool verbose = false;


const char *get_img_file(){
        return img_file;
}

void init_disasm(){

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
        parse_args(argc, argv);

}

