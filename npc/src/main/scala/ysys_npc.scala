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

object ModuleConnect {
    def apply[T <: Data](prevOut: DecoupledIO[T], thisIn: DecoupledIO[T], thisOut: DecoupledIO[T],
                         arch: String = "multi"): Unit = {
        arch match {
            case "multi"  =>   
                prevOut <> thisIn
            case "pipeline" =>
                prevOut.ready := thisIn.ready
                thisIn.bits := RegEnable(prevOut.bits, prevOut.valid && thisIn.ready)
                thisIn.valid := RegEnable(prevOut.valid, thisIn.ready)
            case _        =>   throw new IllegalArgumentException(s"Unsupported architecture")
        }
    }
}


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

    /*ifu.io.in <> wbu.io.out
    idu.io.in <> ifu.io.out
    exu.io.in <> idu.io.out
    lsu.io.in <> exu.io.out 
    wbu.io.in <> lsu.io.out*/
    val stage_arch = "multi"
    ModuleConnect(wbu.io.out, ifu.io.in, ifu.io.out, stage_arch)
    ModuleConnect(ifu.io.out, idu.io.in, idu.io.out, stage_arch)
    ModuleConnect(idu.io.out, exu.io.in, exu.io.out, stage_arch)
    ModuleConnect(exu.io.out, lsu.io.in, lsu.io.out, stage_arch)
    ModuleConnect(lsu.io.out, wbu.io.in, wbu.io.out, stage_arch)

    if(stage_arch == "pipeline"){
        ifu.io.in.valid := true.B
    }

    val regfile = Module(new Regfile(config))
    exu.io.reg_read1 <> regfile.io.read1
    exu.io.reg_read2 <> regfile.io.read2
    wbu.io.reg_write <> regfile.io.write

    val icache = if(config.USE_ICACHE) Some(Module(new ICache(config))) else None
    icache.map { cache =>
        ifu.io.imem <> cache.io.ifu
        cache.io.fencei := ifu.io.fencei
    }


    val axi_arbiter = Module( new AXIArbiter(2, config.axiparams))
    axi_arbiter.io.in(0) <> icache.map(_.io.imem).getOrElse(ifu.io.imem)
    axi_arbiter.io.in(1) <> lsu.io.dmem
    axi_arbiter.io.out <> io.axi


}