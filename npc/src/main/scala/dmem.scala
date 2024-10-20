package top

import chisel3._
import chisel3.util._

class dmem_io extends Bundle{
    val addr = Input(UInt(32.W))
    val wdata = Input(UInt(32.W))
    val rdata = Output(UInt(32.W))
    val we = Input(Bool())
}

class dmem extends Module{
  val io = IO(new dmem_io())

  val memory = Mem(1024*16, UInt(32.W))

  when(io.we) {
    memory.write(io.addr, io.wdata)
  }

  io.rdata := 0.U // default value
  when(! io.we) {
    io.rdata := memory.read((io.addr-0x8000000.U)(9+4,0))  // only use the lowest 14 bits now
  }

}
