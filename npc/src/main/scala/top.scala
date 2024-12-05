package ysyx_23060246

import chisel3._
import circt.stage.ChiselStage
import freechips.rocketchip.amba.axi4._
import cpu._

import AXI4._

object CPUAXI4BundleParameters {
  def apply() = AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4)
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
  val clint = Module(new CLINT)

  val axi_xbar = Module(new AXILiteXbar(2, 
                        Array((0L,0xFFFFFFFFL),(0xa0000048L,0xa0000056L)),
                        32,32))
  val axilite_axi = Module(new AXILite2AXI(32,32))
          
  axi_xbar.io.in <> cpu_npc.io.axi
  axi_xbar.io.out(0) <> axilite_axi.io.axilite 
  axi_xbar.io.out(1) <> clint.io.axi 
  //axilite_axi.io.axi <> io.master

 io.slave <> DontCare 


 val axierror = Module(new AXIError)

 axierror.io.bresp := io.master.bresp
 axierror.io.rresp := io.master.rresp
 axierror.io.wen := io.master.bvalid 
 axierror.io.ren := io.master.arvalid

  io.master.awaddr := axilite_axi.io.axi.aw.bits.addr
  io.master.awvalid := axilite_axi.io.axi.aw.valid
  io.master.awid := axilite_axi.io.axi.aw.bits.id
  io.master.awlen := axilite_axi.io.axi.aw.bits.len
  io.master.awsize := axilite_axi.io.axi.aw.bits.size
  io.master.awburst := axilite_axi.io.axi.aw.bits.burst

  io.master.wvalid := axilite_axi.io.axi.w.valid
  io.master.wdata  := axilite_axi.io.axi.w.bits.data
  io.master.wstrb  := axilite_axi.io.axi.w.bits.strb
  io.master.wlast  := axilite_axi.io.axi.w.bits.last

  io.master.bready := axilite_axi.io.axi.b.ready

  io.master.arvalid := axilite_axi.io.axi.ar.valid
  io.master.araddr  := axilite_axi.io.axi.ar.bits.addr
  io.master.arid    := axilite_axi.io.axi.ar.bits.id
  io.master.arlen   := axilite_axi.io.axi.ar.bits.len
  io.master.arsize  := axilite_axi.io.axi.ar.bits.size
  io.master.arburst := axilite_axi.io.axi.ar.bits.burst

  io.master.rready  := axilite_axi.io.axi.r.ready  
  //----
  axilite_axi.io.axi.aw.ready := io.master.awready

  axilite_axi.io.axi.w.ready := io.master.wready

  axilite_axi.io.axi.b.valid := io.master.bvalid
  axilite_axi.io.axi.b.bits.resp := io.master.bresp 
  axilite_axi.io.axi.b.bits.id   := io.master.bid 

  axilite_axi.io.axi.ar.ready := io.master.arready

  axilite_axi.io.axi.r.valid := io.master.rvalid
  axilite_axi.io.axi.r.bits.resp := io.master.rresp
  axilite_axi.io.axi.r.bits.data := io.master.rdata
  axilite_axi.io.axi.r.bits.last := io.master.rlast
  axilite_axi.io.axi.r.bits.id   := io.master.rid

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