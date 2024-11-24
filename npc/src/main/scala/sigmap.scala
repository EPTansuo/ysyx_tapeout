
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

val map = Array(
	lui     ->  List(PC_4  , A_RS1, B_IMM, ALU_COPY_B, IMM_U, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	auipc   ->  List(PC_4  , A_PC , B_IMM, ALU_ADD   , IMM_U, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	addi    ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	andi    ->  List(PC_4  , A_RS1, B_IMM, ALU_AND   , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	jalr    ->  List(PC_ALU, A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_PC4, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	jal     ->  List(PC_ALU, A_PC , B_IMM, ALU_ADD   , IMM_J, WB_PC4, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	sw      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_S, WB_XX , LD_XX, ST_SW, MASK_W , BR_XX , INST_VALID  ),
	lw      ->  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_XX , LD_LW, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	bgeu    ->  List(PC_ALU, A_PC , B_IMM, ALU_ADD   , IMM_B, WB_XX , LD_XX, ST_XX, MASK_XX, BR_GEU, INST_VALID  ),
	slli    ->  List(PC_4  , A_RS1, B_IMM, ALU_SLL   , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	sltiu   ->  List(PC_4  , A_RS1, B_IMM, ALU_SLTU  , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	add     ->  List(PC_4  , A_RS1, B_RS2, ALU_ADD   , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
	sub     ->  List(PC_4  , A_RS1, B_RS2, ALU_SUB   , IMM_I, WB_ALU, LD_XX, ST_XX, MASK_XX, BR_XX , INST_VALID  ),
)
val 
	default  =  List(PC_4  , A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_XX , LD_XX, ST_XX, MASK_XX, BR_XX , INST_INVALID)

}


