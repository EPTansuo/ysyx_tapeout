package top

import chisel3._
import chisel3.util._
import chisel3.stage._


class top extends Module {
  val io = IO(new Bundle {


  })


}

object topMain extends App {
  (new ChiselStage).emitVerilog(new top, args)
}
