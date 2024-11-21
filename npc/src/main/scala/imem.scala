package top

import chisel3._
import chisel3.util._

class imem_io extends Bundle{
    val addr = Input(UInt(32.W))
    val data = Output(UInt(32.W))
}

class imem extends Module{
  val io = IO(new imem_io())

  val memory = Mem(1024, UInt(32.W))

  io.data := memory.read((io.addr-0x8000000.U)(9,0))  // only use the lowest 10 bits now
}
