package  cpu

import  chisel3._
import chisel3.util._
import chisel3.experimental._

/* 

module DMem(
        input clock,
        input reset,

        input wire we,
        input wire [`InstAddrBus] waddr,
        input wire [`InstAddrBus] raddr,
        input wire [`WordBus] wdata,
        input wire [7:0] wmask,

        output reg [`WordBus] rdata
);
 */
class DMemIO extends Bundle{
  val clock = Input(Clock())
  val reset = Input(Bool())
  val we = Input(Bool())
  val waddr = Input(UInt(32.W))
  val raddr = Input(UInt(32.W))
  val wdata = Input(UInt(32.W))
  val wmask = Input(UInt(8.W))
  val rdata = Output(UInt(32.W))
}


class DMem extends BlackBox with HasBlackBoxPath {
  val io = IO(new DMemIO)
}