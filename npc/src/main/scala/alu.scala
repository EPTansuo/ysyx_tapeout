package cpu

import chisel3._
import chisel3.util._

object aluop extends Enumeration {
  val ALU_ADD, ALU_SUB, ALU_AND, ALU_OR, ALU_XOR,
      ALU_SLL, ALU_SRL, ALU_SRA, ALU_SLT, ALU_SLTU,
      ALU_COPY_A, ALU_COPY_B = Value
}

class aluio(width:Int) extends Bundle {
    val A = input(UInt(width.W)
    val B = input(UInt(width.W)
    val aluop = Input(Uint(4.W))
    val out = Output(UInt(width.W))
}


class alu(val width: Int) extends Module{
    val io = IO(new aluio(width))

    val shamt = io.B(4,0).asUint

    io.out := MuxLookup(io.aluop, io.B)(
        Seq(
            aluop.ALU_ADD.id.U -> (io.A + io.B),
            aluop.ALU_SUB.id.U -> (io.A - io.B),
            aluop.ALU_AND.id.U -> (io.A & io.B),
            aluop.ALU_OR.id.U  -> (io.A | io.B),
            aluop.ALU_XOR.id.U -> (io.A ^ io.B),
            aluop.ALU_SLL.id.U -> (io.A << shamt),
            aluop.ALU_SRL.id.U -> (io.A >> shamt),
            aluop.ALU_SRA.id.U -> ((io.A.asSInt >> shamt).asUInt),
            aluop.ALU_SLT.id.U -> (io.A.asSInt < io.B.asSInt).asUInt,
            aluop.ALU_SLTU.id.U -> (io.A < io.B).asUInt,
            aluop.ALU_COPY_A.id.U -> io.A,
            aluop.ALU_COPY_B.id.U -> io.B
        )
    )
}

