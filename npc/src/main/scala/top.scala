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

class npcIO_2 extends Bundle {
  val interrupt = Input(Bool())
  val master = AXI4Bundle(CPUAXI4BundleParameters())
  val slave = Flipped(AXI4Bundle(CPUAXI4BundleParameters()))
}

class npcIO extends Bundle {
  val interrupt = Input(Bool())
  val master = new AXI4BundleIO(32,32)
  val slave = Flipped(new AXI4BundleIO(32,32))
}


class ysyx_23060246 extends Module {
  val io = IO(new npcIO)
  val cpu_npc = Module(new ysyx_npc(32))
  // val clint = Module(new CLINT)




 io.slave <> DontCare 


  val axi4_conv = Module(new AXI4BundleIFConv(32,32))
  axi4_conv.io.in <> cpu_npc.io.axi
  axi4_conv.io.out <> io.master


 val axierror = Module(new AXIError)

 axierror.io.bresp := io.master.bresp
 axierror.io.rresp := io.master.rresp
 axierror.io.wen := io.master.bvalid 
 axierror.io.ren := io.master.arvalid

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
  ChiselStage.emitSystemVerilogFile(new ysyx_23060246, args, firtoolOptions)
}