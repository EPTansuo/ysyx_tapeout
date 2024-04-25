#include <common.h>
#include <fmt-def.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <utils.h>
#include <string>
#include <regex>

extern char* img_file;

typedef struct{
        word_t pc;
        uint8_t code[4];
        char str[32];
}Disasm;

extern const char *regs[];

Disasm *disasm;

void disassemble(char* logbuf, size_t logbuf_size, word_t pc){
    uint32_t index = (pc - 0x80000000)/4;
    if(disasm[index].pc == pc){
	snprintf(logbuf, logbuf_size, "%s", disasm[index].str);
    }else{
	printf("Error: Can not find disasm for pc: " FMT_WORD_HEX "\n", pc);
    
    }
}


std::string replace_regs_name(const std::string& code) {
    auto result = code;	
    for(auto i=31; i>0; i--){
    	result = std::regex_replace(result, std::regex(" x"+ std::to_string(i)), (std::string)" " + regs[i]); 
	result = std::regex_replace(result, std::regex(",x"+ std::to_string(i)), (std::string)"," + regs[i]); 
    }
    return result;
}




void init_disasm(){
	char img_file_path[200];
	char line[100];
	char asm_code_buf[100];
	char img_full_name[50];
	uint16_t lines = 0;
	strcpy(img_full_name, img_file);
	char* img_basename = basename(img_full_name);
	strcat(img_basename,".disasm");
	sprintf(img_file_path,"%s/%s","./build",img_basename);

	FILE* fp = fopen(img_file_path,"r");

	if(fp == NULL){
		printf("Error while open file: %s %s:%d\n",img_file_path,__FILE__,__LINE__);
		return;
	}

	while (fgets(line, sizeof(line), fp) != NULL) {
        	lines++;  //统计文件的行
    	}

	disasm = (Disasm*)malloc(sizeof(Disasm)*lines);
	fseek(fp, 0, SEEK_SET);
	int i =0;
	while (fgets(line, sizeof(line), fp) != NULL) {
		//if (sscanf(line, "%x %99[^\n]", &disasm[i].pc, &(disasm[i].str[0])) == 2) {
		if (sscanf(line, "%x %99[^\n]", &disasm[i].pc, asm_code_buf) == 2) {
		//printf("Address: 0x%X, Instruction: %s\n", disasm[i].pc, disasm[i].str);
			strcpy(&(disasm[i].str[0]),replace_regs_name(asm_code_buf).c_str());
		} else {
		fprintf(stderr, "Failed to parse line: %s", line);
		}
	i++;
    	}

}