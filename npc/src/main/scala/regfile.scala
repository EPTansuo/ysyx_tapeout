package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._

class RegfileReadIO(xlen:Int) extends Bundle{
    var addr = Input(UInt(5.W))
    var data = Output(UInt(xlen.W))
}

class RegfileWriteIO(xlen:Int) extends Bundle{
    var en = Input(Bool())
    var addr = Input(UInt(5.W))
    var data = Input(UInt(xlen.W))
}

class RegfileIO(xlen: Int) extends Bundle{
    val read1 = new RegfileReadIO(xlen)
    val read2 = new RegfileReadIO(xlen)
    val write = new RegfileWriteIO(xlen)
}


class Regfile(xlen: Int) extends Module{
    val io = IO(new RegfileIO(xlen))

    var regs = Mem(32, UInt(xlen.W))

    io.read1.data := regs(io.read1.addr)
    io.read2.data := regs(io.read2.addr)

    when(io.write.en) {
        regs(io.write.addr) := Mux(io.write.addr === 0.U, 0.U, io.write.data)
    }
}
