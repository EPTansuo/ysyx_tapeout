package npc

import chisel3._
import chisel3.util._

import AXI4._

/*
module Mem(
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
class MemIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val we    = Input(Bool())
  val waddr = Input(UInt(32.W))
  val raddr = Input(UInt(32.W))
  val wdata = Input(UInt(32.W))
  val wmask = Input(UInt(8.W))
  val rdata = Output(UInt(32.W))
}

class Mem extends BlackBox with HasBlackBoxPath {
  val io = IO(new MemIO)
}

class LFSR4 extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(4.W))
  })

  val lfsr = RegInit(1.U(4.W))

  lfsr := Cat(lfsr(0) ^ lfsr(1) ^ lfsr(2), lfsr(3, 1))

  io.out := lfsr
}

class SRAM extends Module {
  val io = IO(new Bundle {
    val axi = AXILiteSlaveIF(addrWidthBits = 32, dataWidthBits = 32)
  })

  val axi    = io.axi
  val mem    = Module(new Mem)
  val lfsr_r  = Module(new LFSR4)
  val random_r = lfsr_r.io.out
  val lfsr_w = Module(new LFSR4)
  val random_w = lfsr_w.io.out

  val s_read_idle :: s_read :: s_read_delay :: s_wait_ready :: Nil = Enum(4)

  val state_r = RegInit(s_read_idle)
  state_r := MuxLookup(state_r, s_read_idle, Seq(
    s_read_idle -> Mux(axi.ar.valid, s_read, s_read_idle),
    s_read -> s_read_delay,
    s_read_delay -> Mux(random_r >= 8.U , s_wait_ready, s_read_delay),
    s_wait_ready -> Mux(axi.r.ready, s_read_idle, s_wait_ready)
  ))

  axi.ar.ready := state_r === s_read_idle
  val rdata = RegInit(0.U(32.W))
  when(state_r == s_read === 0.U){
    rdata := mem.io.rdata
  }
  axi.r.valid := state_r === s_wait_ready
  axi.r.bits.data := rdata 
  axi.r.bits.resp := 0.U


  
}
