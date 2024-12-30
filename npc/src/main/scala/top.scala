package ysyx_23060246

import chisel3._
import circt.stage.ChiselStage
import freechips.rocketchip.amba.axi4._
import cpu._
import defines._
import AXI4._

object CPUAXI4BundleParameters {
  def apply() = AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = AXI_IDBITS)
}

class npcIO extends Bundle {
  val interrupt = Input(Bool())
  val master = new AXIBundle(32,32)
  val slave = Flipped(new AXIBundle(32,32))
}


class ysyx_23060246(params: AXI4BundleParameters) extends Module {
  val io = IO(new npcIO)
  val cpu_npc = Module(new ysyx_npc(32))
  val clint = Module(new CLINT(params))


  val xbar = Module(new AXIXbar(2, 
                        Array((0L,0xFFFFFFFFL),(0x02000000L,0x0200ffffL)),
                        CPUAXI4BundleParameters()))
  xbar.io.in <> cpu_npc.io.axi

  if(defines.USE_SOC){
    val axi4_conv = Module(new AXI4BundleIFConv(32,32))
    xbar.io.out(0) <> axi4_conv.io.in
    axi4_conv.io.out <> io.master
  } else {
    val sram = Module(new SRAM(params))
    xbar.io.out(0) <> sram.io.axi
    io.master <> DontCare 
  }

  xbar.io.out(1) <> clint.io.axi 


  val axierror = Module(new AXIError)
  axierror.io.bresp := io.master.bresp
  axierror.io.rresp := io.master.rresp
  axierror.io.wen := io.master.bvalid 
  axierror.io.ren := io.master.arvalid


  io.slave <> DontCare 
  dontTouch(io.slave)
  dontTouch(io.interrupt)
  dontTouch(io.master)

}

object npcMain extends App {
  // val firtoolOptions = Array("--disable-annotation-unknown")
  val firtoolOptions = Array("--lowering-options=" + List(
        // make yosys happy
        // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
        "disallowLocalVariables",
        "disallowPackedArrays",
        "locationInfoStyle=wrapInAtSquareBracket"
    ).reduce(_ + "," + _),
    "--disable-annotation-unknown")
  ChiselStage.emitSystemVerilogFile(new ysyx_23060246(CPUAXI4BundleParameters()), args, firtoolOptions)
}