package cpu

import chisel3._
import chisel3.util._

object aluop {
  val ALU_ADD     = 0.U(4.W)
  val ALU_SUB     = 1.U(4.W)
  val ALU_AND     = 2.U(4.W)
  val ALU_OR      = 3.U(4.W)
  val ALU_XOR     = 4.U(4.W)
  val ALU_SLL     = 5.U(4.W)
  val ALU_SRL     = 6.U(4.W)
  val ALU_SRA     = 7.U(4.W)
  val ALU_SLT     = 8.U(4.W)
  val ALU_SLTU    = 9.U(4.W)
  val ALU_COPY_A  = 10.U(4.W)
  val ALU_COPY_B  = 11.U(4.W)
}

class ALUIO(width:Int) extends Bundle {
    val A = Input(UInt(width.W))
    val B = Input(UInt(width.W))
    val aluop = Input(UInt(4.W))
    val out = Output(UInt(width.W))
}


class ALU(val width: Int) extends Module{
    val io = IO(new ALUIO(width))

    // Support 32bit and 64bit
    val shamt = if(width == 32) io.B(4,0).asUInt else io.B(5,0).asUInt

    io.out := MuxLookup(io.aluop, io.B,
        Seq(
            aluop.ALU_ADD -> (io.A + io.B),
            aluop.ALU_SUB -> (io.A - io.B),
            aluop.ALU_AND -> (io.A & io.B),
            aluop.ALU_OR  -> (io.A | io.B),
            aluop.ALU_XOR -> (io.A ^ io.B),
            aluop.ALU_SLL -> (io.A << shamt),
            aluop.ALU_SRL -> (io.A >> shamt),
            aluop.ALU_SRA -> ((io.A.asSInt >> shamt).asUInt),
            aluop.ALU_SLT -> (io.A.asSInt < io.B.asSInt).asUInt,
            aluop.ALU_SLTU -> (io.A < io.B).asUInt,
            aluop.ALU_COPY_A -> io.A,
            aluop.ALU_COPY_B -> io.B
        )
    )
}

