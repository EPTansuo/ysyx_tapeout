package cpu

import chisel3._
import chisel3.util._

import  defines._

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
