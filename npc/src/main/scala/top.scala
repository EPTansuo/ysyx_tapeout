package npc

import chisel3._
import chisel3.util._
import chisel3.stage._
import cpu._

import AXI4._

class npcIO extends Bundle {
}


class npc extends Module {
  val io = IO(new npcIO)
  val cpu = Module(new ysyx_npc(32))
  val sram = Module(new SRAM)
  val uart = Module(new UART_AXILite)
  val clint = Module(new CLINT)

  val axi_xbar = Module(new AXILiteXbar(3, 
                        Array((0L,0xFFFFFFFFL),(0xa0000048L,0xa000004FL),(0xa0000048L,0xa0000056L)),
                        32,32))
  axi_xbar.io.in <> cpu.io.axi
  axi_xbar.io.out(0) <> sram.io.axi
  axi_xbar.io.out(1) <> uart.io.axi
  axi_xbar.io.out(2) <> clint.io.axi  
}

object npcMain extends App {
  (new ChiselStage).emitVerilog(new npc, args)
}
