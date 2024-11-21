#include <common.h>
#include <verilated.h>
#include <Vcpu.h>
#include <Vcpu_inst_rom.h>
#include <Vcpu_ifu.h>
#include <Vcpu_cpu.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <color.h>
#include <iomanip>
#include <memory/paddr.h>

//const char * img_file = NULL;

// addi x1 x0 1  ; x1 = 1
// addi x2 x0 2  ; x2 = 2 
// addi x2 x1 9  ; x2 = x1 + 9
static uint32_t img_default[] = {           //    imm          rs1       rd   opcode
	0b00000000000100000000000010010011, // 0b000000000001 00000 000 00001 0010011,
	0b00000000001000000000000100010011, // 0b000000000010 00000 000 00010 0010011
	0b00000000100100001000000100010011, // 0b000000001001 00001 000 00010 0010011
	0b00000000000100000000000001110011  // ebreak
};

uint32_t inst_num;
char img[131072];
extern char* img_file;
extern Vcpu *top;

long init_insts_file(){
	long size = 0;
	FILE* fp = fopen(img_file,"r");
	if(fp == NULL){
		printf(L_RED "Can not open init insts!\n" NONE);
		return 0;
	}
	fseek(fp, 0, SEEK_SET);
	size = fread(img, 1, 131072, fp);
	inst_num = size / 4;
	for(size_t i = 0; i < 131072; i++){
		top->cpu->ifu1->inst_rom1->insts[i] = img[i];
	}
	fclose(fp);	
	return size;
}

long init_insts_default(){
	
	//uint8_t *insts = new uint8_t[sizeof(img)];
	long size = sizeof(img_default);

	inst_num = size/sizeof(uint32_t);

	for (size_t i = 0; i < inst_num; i++)
	{
		for(size_t j=0; j < 4; j++){
			top->cpu->ifu1->inst_rom1->insts[i*4+j] = img_default[i] >> (j*8) & 0xff;
		}
	}

	return size;
}

void print_inst(word_t pc)
{	
	VlUnpacked<unsigned char, 131072>& insts = (top->cpu->ifu1->inst_rom1->insts);
	word_t index = pc - 0x80000000;
	std::cout << std::hex << std::setw(8) << std::setfill('0')
		<< pc << ":    ";
	std::cout << std::hex << std::setw(2) << std::setfill('0')
		<< static_cast<int>(insts[index+3]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+2]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+1]) << " "
		<< std::setw(2) << static_cast<int>(insts[index+0]);
}

void print_all_insts(){
	std::cout<<"------------------"<<std::endl;

	for (size_t i = 0; i < inst_num; ++i) {
		print_inst(0x80000000 + i*4);
		printf("\n");
	}
	std::cout<<"------------------"<<std::endl;
}


long load_img(){
	if(top == NULL){
		top = new Vcpu;
	}
	if(img_file == NULL){
		return init_insts_default();
	}
	else{
		init_insts_file();
		if (img_file == NULL) {
		Log("No image is given. Use the default build-in image.");
		return 4096; // built-in image size
		}

		FILE *fp = fopen(img_file, "rb");
		Assert(fp, "Can not open '%s'", img_file);

		fseek(fp, 0, SEEK_END);
		long size = ftell(fp);

		Log("The image is %s, size = %ld", img_file, size);

		fseek(fp, 0, SEEK_SET);
		int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
		assert(ret == 1);

		fclose(fp);
		return size;
		
	}
}