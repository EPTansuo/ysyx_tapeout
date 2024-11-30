package cpu

import chisel3._
import chisel3.util._

import  defines._

import AXILiteDefs._ 

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

  mem.io.pc := io.writeAddr.bits.addr
  io.readData.bits.data := mem.io.data

  io.readAddr.ready := true.B
  io.readData.valid := io.readAddr.valid  //用的DPI-C，是可以立刻有效的
  io.readData.bits.resp := 0.U

  io.writeAddr.ready := false.B
  io.writeData.ready := false.B
  io.writeResp.valid := false.B
  //Imem不需要写数据，所以不用继续操作了

}