package cpu

import chisel3._
import chisel3.util._
import insts._

object pc_sel extends Enumeration {
  val PC_4, PC_0, PC_ALU = Value
}

object A_sel extends Enumeration {
  val A_PC, A_RS1 = Value
}

object B_sel extends Enumeration {
  val B_IMM, B_RS2 = Value
}

object wb_sel extends Enumeration {
  val WB_ALU, WB_MEM, WB_PC , WB_XX= Value
}

object control {


    import pc_sel._
    import A_sel._
    import B_sel._
    import imm_sel._
    import aluop._ 
    import wb_sel._ 

  val map = Array(
        lui -> List(PC_4.id.U, A_PC.id.U, A_PC.id.U, ALU_COPY_B.id.U, IMM_U.id.U, WB_ALU.id.U),
        auipc -> List(PC_4.id.U, A_PC.id.U, A_PC.id.U, ALU_ADD.id.U, IMM_U.id.U, WB_ALU.id.U),
    )
    val default = List(PC_4.id.U, A_RS1.id.U, B_IMM.id.U, ALU_ADD.id.U, IMM_I.id.U, WB_XX.id.U)
}

class control_io(xlen: Int) extends Bundle{
    val inst = Input(UInt(xlen.W))
    val pc_sel = Output(UInt(2.W))

    val A_sel = Output(UInt(1.W))
    val B_sel = Output(UInt(1.W))
    val alu_op = Output(UInt(4.W))

    val imm_sel = Output(UInt(3.W))

    val wb_sel = Output(UInt(2.W))

}

class control(xlen: Int) extends Module{
    val io = IO(new control_io(xlen))
    val ctrlsig = ListLookup(io.inst, control.default, control.map)
    io.pc_sel := ctrlsig(0)
    io.A_sel := ctrlsig(1)
    io.B_sel := ctrlsig(2)
    io.alu_op := ctrlsig(3)
    io.imm_sel := ctrlsig(4)
    io.wb_sel := ctrlsig(5)
}


