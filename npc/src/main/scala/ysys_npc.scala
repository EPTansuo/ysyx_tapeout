package cpu

import AXI4._
import chisel3._
import chisel3.util._
//import defines._ 
import freechips.rocketchip.amba.axi4._
import org.chipsalliance.cde.config.Parameters
// object ModuleConnect {
//   def apply(left: Module, right: Module, isPipe: Boolean = false): Unit = {
//     isPipe match {
//       case false  =>   right.io.in <> left.io.out
//       case _        =>   throw new IllegalArgumentException(s"Unsupported architecture")
//     }
//   }
// }

class ysyx_npc(config: NPCConfig) extends Module {
    val io = IO(new Bundle {
        // val imem = Flipped(new IMemIO(xlen))
        // val dmem = Flipped(new DMemIO())
        //val axi = new AXILiteMasterIF(addrWidthBits = 32, dataWidthBits = 32)
        val axi = new AXI4Bundle(config.axiparams)
    })

    val ifu = Module(new IFU(config))
    val idu = Module(new IDU(config))
    val exu = Module(new EXU(config))
    val lsu = Module(new LSU(config))
    val wbu = Module(new WBU(config))

    ifu.io.in <> wbu.io.out
    idu.io.in <> ifu.io.out
    exu.io.in <> idu.io.out
    lsu.io.in <> exu.io.out 
    wbu.io.in <> lsu.io.out


    val regfile = Module(new Regfile(config))
    exu.io.reg_read1 <> regfile.io.read1
    exu.io.reg_read2 <> regfile.io.read2
    wbu.io.reg_write <> regfile.io.write

    //val icache = Module(new ICache(config))
    //ifu.io.imem <> icache.io.ifu 

    val axi_arbiter = Module( new AXIArbiter(2, config.axiparams))
    //axi_arbiter.io.in(0) <> icache.io.imem
    axi_arbiter.io.in(0) <> ifu.io.imem
    axi_arbiter.io.in(1) <> lsu.io.dmem
    axi_arbiter.io.out <> io.axi


}