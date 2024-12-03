package ysyx_23060246

import chisel3._
import chisel3.util._
import chisel3.stage._
import cpu._

import AXI4._

class npcIO extends Bundle {
  val interrupt = Input(Bool())
  val master = new AXI4Bundle(32,32)
  val slave = Flipped(new AXI4Bundle(32,32))
}


class ysyx_23060246 extends Module {
  val io = IO(new npcIO)
  val cpu_npc = Module(new ysyx_npc(32))
  val clint = Module(new CLINT)

  val axi_xbar = Module(new AXILiteXbar(2, 
                        Array((0L,0xFFFFFFFFL),(0xa0000048L,0xa0000056L)),
                        32,32))
  val axilite_axi = Module(new AXILite2AXI(32,32))
          
  axi_xbar.io.in <> cpu_npc.io.axi
  axi_xbar.io.out(0) <> axilite_axi.io.axilite 
  axi_xbar.io.out(1) <> clint.io.axi 
  axilite_axi.io.axi <> io.master

 io.slave <> DontCare 
}

object npcMain extends App {
  (new ChiselStage).emitVerilog(new ysyx_23060246, args)
}
