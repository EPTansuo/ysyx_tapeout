package npc

import chisel3._
import chisel3.util._
import chisel3.stage._
import cpu._


class npcIO extends Bundle {
}


class npc extends Module {
  val io = IO(new npcIO)
  val cpu = Module(new ysyx_npc(32))
  val sram = Module(new SRAM)

  sram.io.axi <> cpu.io.axi

}

object npcMain extends App {
  (new ChiselStage).emitVerilog(new npc, args)
}
