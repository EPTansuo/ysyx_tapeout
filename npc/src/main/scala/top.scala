package ysyx_23060246

import chisel3._
import circt.stage.ChiselStage
import freechips.rocketchip.amba.axi4._
import cpu._
//import defines._
import AXI4._
import io.circe._ 
import io.circe.generic.auto._
import io.circe.parser._



class npcIO extends Bundle {
  val interrupt = Input(Bool())
  val master = new AXIBundle(32,32)
  val slave = Flipped(new AXIBundle(32,32))
}


class ysyx_23060246(config: NPCConfig) extends Module {
  val io = IO(new npcIO)
  val cpu_npc = Module(new ysyx_npc(config))
  val clint = Module(new CLINT(config.axiparams))


  val xbar = Module(new AXIXbar(2, 
                        Array((0L,0xFFFFFFFFL),(config.CLINT_BASE,config.CLINT_END)) ,
                        config.axiparams))
  xbar.io.in <> cpu_npc.io.axi

  if(config.USE_SOC){
    val axi4_conv = Module(new AXI4BundleIFConv(32,32))
    xbar.io.out(0) <> axi4_conv.io.in
    axi4_conv.io.out <> io.master
  } else {
    val sram = Module(new SRAM( config.axiparams))
    val uart = Module(new UART_AXI( config.axiparams))
    val xbar_2 = Module(new AXIXbar(2, 
                        Array((0L,0xFFFFFFFFL),(0xa00003f0L,0xa00003ffL)) ,
                         config.axiparams))
    xbar.io.out(0) <> xbar_2.io.in 
    xbar_2.io.out(0) <> sram.io.axi 
    xbar_2.io.out(1) <> uart.io.axi
    io.master <> DontCare 
  }

  xbar.io.out(1) <> clint.io.axi 


  // val axierror = Module(new AXIError)
  // axierror.io.bresp := io.master.bresp
  // axierror.io.rresp := io.master.rresp
  // axierror.io.wen := io.master.bvalid 
  // axierror.io.ren := io.master.arvalid


  io.slave <> DontCare 
  dontTouch(io.slave)
  dontTouch(io.interrupt)
  dontTouch(io.master)

}

object npcMain extends App {
  
  val config = NPCConfig()
  println(config.asString)

  val firtoolOptions = Array("--lowering-options=" + List(
        // make yosys happy
        // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
        "disallowLocalVariables",
        "disallowPackedArrays",
        "locationInfoStyle=wrapInAtSquareBracket"
    ).reduce(_ + "," + _),
    "--disable-annotation-unknown")
  ChiselStage.emitSystemVerilogFile(new ysyx_23060246(config), args, firtoolOptions)
}