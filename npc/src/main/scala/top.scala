package top

import chisel3._
import chisel3.util._
import chisel3.stage._
import cpu._

class top extends Module {
  val io = IO(new Bundle {
    var cpu_port = new cpu_io()

  })
  val cpu_ = Module(new cpu())
  val imem_ = Module(new imem())
  val idmem_ = Module(new dmem())

  cpu_.io.imem <> imem_.io
  cpu_.io.dmem <> idmem_.io
  io.cpu_port <> cpu_.io
}

object topMain extends App {
  (new ChiselStage).emitVerilog(new top, args)
}
