package cpu

import AXI4._
import chisel3._
import chisel3.util._
import defines._ 

// object ModuleConnect {
//   def apply(left: Module, right: Module, isPipe: Boolean = false): Unit = {
//     isPipe match {
//       case false  =>   right.io.in <> left.io.out
//       case _        =>   throw new IllegalArgumentException(s"Unsupported architecture")
//     }
//   }
// }

class ysyx_npc(xlen:Int) extends Module {
    val io = IO(new Bundle {
        // val imem = Flipped(new IMemIO(xlen))
        // val dmem = Flipped(new DMemIO())
        val axi = new AXILiteMasterIF(addrWidthBits = 32, dataWidthBits = 32)
    })

    val ifu = Module(new IFU(xlen))
    val idu = Module(new IDU(xlen))
    val exu = Module(new EXU(xlen))
    val lsu = Module(new LSU(xlen))
    val wbu = Module(new WBU(xlen))

    ifu.io.in <> wbu.io.out
    idu.io.in <> ifu.io.out
    exu.io.in <> idu.io.out
    lsu.io.in <> exu.io.out 
    wbu.io.in <> lsu.io.out


    val regfile = Module(new Regfile(xlen))
    exu.io.reg_read1 <> regfile.io.read1
    exu.io.reg_read2 <> regfile.io.read2
    wbu.io.reg_write <> regfile.io.write


    val axi4lite_arbiter = Module(new AXI4LiteArbiter(2, 32, 32))

    axi4lite_arbiter.io.masters(0) <> ifu.io.imem
    axi4lite_arbiter.io.masters(1) <> lsu.io.dmem
    axi4lite_arbiter.io.slave <> io.axi

}