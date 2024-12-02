package cpu

import chisel3._
import chisel3.util._

import defines._
import AXI4._

/* 
module IMem(
        input reset,
        input pc,
        output reg [`WordBus] data
);
 */

class IMemIO(xlen: Int) extends Bundle{
  val reset = Input(Bool())
  val pc = Input(UInt(xlen.W))
  val data = Output(UInt(32.W))
}


class IMem(xlen: Int) extends BlackBox with HasBlackBoxPath {
  val io = IO(new IMemIO(xlen))
}



class IMem_AIXLite(xlen: Int) extends Module{
  val io = IO(new AXILiteSlaveIF(xlen, 32))

  val mem = Module(new IMem(xlen))

  mem.io.pc := io.aw.bits.addr
  io.r.bits.data := mem.io.data

  io.ar.ready := true.B
  io.r.valid := io.ar.valid  //用的DPI-C，是可以立刻有效的
  io.r.bits.resp := 0.U

  io.aw.ready := false.B
  io.w.ready := false.B
  io.b.valid := false.B
  //Imem不需要写数据，所以不用继续操作了

}