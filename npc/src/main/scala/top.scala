package npc

import chisel3._
import chisel3.util._
import chisel3.stage._
import cpu._


class npcIO extends Bundle {
}


class npc extends Module {
  val io = IO(new npcIO)
  val cpu = Module(new CPU(32))
  val imem = Module(new IMem(32))
  val dmem = Module(new DMem())
  cpu.io.imem <> imem.io
  cpu.io.dmem <> dmem.io

  cpu.clock := clock
  cpu.reset := reset
}

object npcMain extends App {
  (new ChiselStage).emitVerilog(new npc, args)
}
