package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._

class regfile_io(xlen: Int) extends Bundle{
    var raddr1 = Input(UInt(5.W))
    var raddr2 = Input(UInt(5.W))
    var rdata1 = Output(UInt(xlen.W))
    var rdata2 = Output(UInt(xlen.W))
    var we = Input(Bool())
    var waddr = Input(UInt(5.W))
    var wdata = Input(UInt(xlen.W))
}


class regfile(xlen: Int) extends Module{
    val io = IO(new regfile_io(xlen))

    var regs = Mem(32, UInt(xlen.W))
    io.rdata1 := Mux(io.raddr1 === 0.U, 0.U, regs(io.raddr1))
    io.rdata2 := Mux(io.raddr1 === 0.U, 0.U, regs(io.raddr2))

    when(io.we) {
        regs(io.waddr) := io.wdata
    }
}
