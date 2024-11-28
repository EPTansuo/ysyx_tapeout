
/****************************************************
*
* Automatically generated file; DO NOT EDIT.
*
*****************************************************/

package  cpu


import chisel3._
import chisel3.util._
import insts._

object SigMap{


  import pc_sel._
  import A_sel._
  import B_sel._
  import imm_sel._
  import aluop._
  import wb_sel._
  import valid._
  import mask_sel._
  import st_sel._
  import ld_sel._
  import br_sel._
  import csr_cmd._

val map = Array(
	lui     ->  List(PC_4  , A_RS1, B_IMM, ALU_COPY_B, IMM_U, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	auipc   ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_U, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	addi    ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	andi    ->  List(PC_4  , A_RS1, B_IMM, ALU_AND   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	ori     ->  List(PC_4  , A_RS1, B_IMM, ALU_OR    , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	xori    ->  List(PC_4  , A_RS1, B_IMM, ALU_XOR   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	jalr    ->  List(PC_ALU, A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_PC4, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	jal     ->  List(PC_ALU, A_PC , B_IMM, ALU_ADD   , IMM_J, WB_PC4, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sb      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_S, WB_XX , LD_XX , ST_SB, MASK_B , BR_XX , CSR_XX, INST_VALID  ),
	sh      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_S, WB_XX , LD_XX , ST_SH, MASK_H , BR_XX , CSR_XX, INST_VALID  ),
	sw      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_S, WB_XX , LD_XX , ST_SW, MASK_W , BR_XX , CSR_XX, INST_VALID  ),
	lw      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_MEM, LD_LW , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	lhu     ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_MEM, LD_LHU, ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	lh      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_MEM, LD_LH , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	lb      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_MEM, LD_LB , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	lbu     ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_MEM, LD_LBU, ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	bne     ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_NE , CSR_XX, INST_VALID  ),
	beq     ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_EQ , CSR_XX, INST_VALID  ),
	blt     ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_LT , CSR_XX, INST_VALID  ),
	bltu    ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_LTU, CSR_XX, INST_VALID  ),
	bgeu    ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_GEU, CSR_XX, INST_VALID  ),
	bge     ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX , ST_XX, MASK_XX, BR_GE , CSR_XX, INST_VALID  ),
	srli    ->  List(PC_4  , A_RS1, B_IMM, ALU_SRL   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	srai    ->  List(PC_4  , A_RS1, B_IMM, ALU_SRA   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	srl     ->  List(PC_4  , A_RS1, B_RS2, ALU_SRL   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sra     ->  List(PC_4  , A_RS1, B_RS2, ALU_SRA   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	slli    ->  List(PC_4  , A_RS1, B_IMM, ALU_SLL   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sltu    ->  List(PC_4  , A_RS1, B_RS2, ALU_SLTU  , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sltiu   ->  List(PC_4  , A_RS1, B_IMM, ALU_SLTU  , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sll     ->  List(PC_4  , A_RS1, B_RS2, ALU_SLL   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	add     ->  List(PC_4  , A_RS1, B_RS2, ALU_ADD   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	sub     ->  List(PC_4  , A_RS1, B_RS2, ALU_SUB   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	xor     ->  List(PC_4  , A_RS1, B_RS2, ALU_XOR   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	and     ->  List(PC_4  , A_RS1, B_RS2, ALU_AND   , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	or      ->  List(PC_4  , A_RS1, B_RS2, ALU_OR    , IMM_I, WB_ALU, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_VALID  ),
	csrrw   ->  List(PC_4  , A_RS1, B_RS2, ALU_ADD   , IMM_I, WB_CSR, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_W , INST_VALID  ),
	csrrs   ->  List(PC_4  , A_RS1, B_RS2, ALU_ADD   , IMM_I, WB_CSR, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_S , INST_VALID  ),
	ecall   ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_CSR, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_P , INST_VALID  ),
	mret    ->  List(PC_EPC, A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_CSR, LD_XX , ST_XX, MASK_XX, BR_XX , CSR_P , INST_VALID  ),
)
val 
	default  =  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_XX , LD_XX , ST_XX, MASK_XX, BR_XX , CSR_XX, INST_INVALID)

}


