#include <unistd.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include "monitor.h"

static char* img_file  = NULL;

const char *get_img_file(){
        return img_file;
}


static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-h", table, NULL)) != -1) {
    switch (o) {
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char** argv){
        parse_args(argc, argv);

}

