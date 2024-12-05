package AXI4


import chisel3._
import chisel3.util._

class AXILite2AXI(addrWidthBits: Int, dataWidthBits: Int) extends Module{
    val io = IO(new Bundle{
        val axilite = new AXILiteSlaveIF(addrWidthBits,dataWidthBits)
        val axi = new AXI4BundleIO(addrWidthBits,dataWidthBits)
    })

    val axi = io.axi
    val axilite = io.axilite

  axi.awaddr := axilite.aw.bits.addr
  axi.awvalid := axilite.aw.valid
  axilite.aw.ready := axi.awready

  axi.wdata := axilite.w.bits.data
  axi.wstrb := axilite.w.bits.strb
  axi.wvalid := axilite.w.valid
  axilite.w.ready := axi.wready

  axi.bready := axilite.b.ready
  axilite.b.bits := axi.bresp
  axilite.b.valid := axi.bvalid

  axi.araddr := axilite.ar.bits.addr
  axi.arvalid := axilite.ar.valid
  axilite.ar.ready := axi.arready

  axi.rready := axilite.r.ready
  axilite.r.valid := axi.rvalid
  axilite.r.bits.data := axi.rdata
  axilite.r.bits.resp := axi.rresp

  axi.awlen := 0.U
  axi.awsize := 2.U
  axi.awburst := 0.U
  axi.wlast := true.B
  axi.awid := 0.U
  axi.arburst := 0.U
  axi.arlen := 0.U
  axi.arsize := 2.U
  axi.arburst := 0.U
 // axi.rlast := true.B
  axi.arid := 0.U

}