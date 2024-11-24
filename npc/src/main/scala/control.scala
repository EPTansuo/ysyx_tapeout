package cpu

import chisel3._
import chisel3.util._
import insts._

object pc_sel {
  val PC_4  = 0.U(2.W)
  val PC_0  = 1.U(2.W)
  val PC_ALU = 2.U(2.W)
}

object A_sel {
  val A_PC  = 0.U(1.W)
  val A_RS1 = 1.U(1.W)
}

object B_sel {
  val B_IMM  = 0.U(2.W)
  val B_RS2  = 1.U(2.W)
}

object imm_sel {
  val IMM_I = 0.U(3.W)
  val IMM_U = 1.U(3.W)
  val IMM_S = 2.U(3.W)
  val IMM_J = 3.U(3.W)
  val IMM_B = 4.U(3.W)
}

// wb from alu, mem, pc or xx
object wb_sel {
  val WB_ALU = 0.U(2.W)
  val WB_MEM = 1.U(2.W)
  val WB_PC  = 2.U(2.W)
  val WB_XX  = 3.U(2.W)
}

object st_sel {
  val ST_SW  = 0.U(2.W)
  val ST_SH  = 1.U(2.W)
  val ST_SB  = 2.U(2.W)
  val ST_XX  = 3.U(2.W)
}

object ld_sel {
  val LD_LW  = 0.U(3.W)
  val LD_LH  = 1.U(3.W)
  val LD_LB  = 2.U(3.W)
  val LD_LBU = 3.U(3.W)
  val LD_LHU = 4.U(3.W)
  val LD_XX  = 5.U(3.W)
}

object mask_sel {
  val MASK_XX = 0x0.U(8.W)
  val MASK_B  = 0x01.U(8.W)
  val MASK_H  = 0x03.U(8.W)
  val MASK_W  = 0x0f.U(8.W)
}

object valid {
  val INST_VALID   = 0.U(1.W)
  val INST_INVALID = 1.U(1.W)
}


object Control {


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


val map = Array(
        lui     ->  List(PC_4, A_PC , B_IMM, ALU_COPY_B, IMM_U, WB_XX , LD_XX, WB_XX, MASK_XX, INST_VALID  ),
        auipc   ->  List(PC_4, A_PC , B_IMM, ALU_ADD   , IMM_U, WB_XX , LD_XX, WB_XX, MASK_XX, INST_VALID  ),
        addi    ->  List(PC_4, A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_ALU, LD_XX, WB_XX, MASK_XX, INST_VALID  ),
)
val 
        default  =  List(PC_4, A_RS1, B_IMM, ALU_ADD   , IMM_I, WB_XX , LD_XX, WB_XX, MASK_XX, INST_INVALID)


}

class ControlOut(xlen: Int) extends Bundle{
  val pc_sel = Output(UInt(2.W))
  val A_sel = Output(UInt(1.W))
  val B_sel = Output(UInt(1.W))
  val alu_op = Output(UInt(4.W))
  val imm_sel = Output(UInt(3.W))
  val wb_sel = Output(UInt(2.W))
  val ld_sel = Output(UInt(3.W))
  val st_sel = Output(UInt(2.W))
  val mask_sel = Output(UInt(3.W))
}

class ControlIn(xlen: Int) extends Bundle{
    val inst = Input(UInt(xlen.W))
}

class Control(xlen: Int) extends Module{
  val io = IO(new Bundle{
    val out = Output(new ControlOut(xlen))
    val in = Input(new ControlIn(xlen))
  })
  val ctrlsig = ListLookup(io.in.inst, Control.default, Control.map)
  io.out.pc_sel := ctrlsig(0)
  io.out.B_sel := ctrlsig(2)
  io.out.A_sel := ctrlsig(1)
  io.out.alu_op := ctrlsig(3)
  io.out.imm_sel := ctrlsig(4)
  io.out.wb_sel := ctrlsig(5)
  io.out.ld_sel := ctrlsig(6)
  io.out.st_sel := ctrlsig(7)
  io.out.mask_sel := ctrlsig(8)

  //Ebreak
  val ebreak_ = Module(new Ebreak)
  val isebreak = io.in.inst === insts.ebreak
  ebreak_.io.isebreak := isebreak

  //invaild instruction
  val instInvalid = Module(new InstInvalid)
  instInvalid.io.isvalid := (ctrlsig(9) === valid.INST_VALID) || isebreak
}


