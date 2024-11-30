package cpu


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
        val imem = Flipped(new IMemIO(xlen))
        val dmem = Flipped(new DMemIO())
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


    //val pc = RegInit(PC_INIT - 4.U(xlen.W))
    val pc = RegInit(PC_INIT)
    when(exu.io.out.valid){
        pc := exu.io.npc 
    }
    ifu.io.pc_in := pc 

    val regfile = Module(new Regfile(xlen))
    exu.io.reg_read1 <> regfile.io.read1
    exu.io.reg_read2 <> regfile.io.read2
    wbu.io.reg_write <> regfile.io.write


    val csr = Module(new CSR(xlen))
    csr.io.cmd := exu.io.csr_cmd
    csr.io.inst := exu.io.csr_inst
    csr.io.pc := pc
    csr.io.in := wbu.io.csr_in
    exu.io.csr_pc := csr.io.target_pc
    wbu.io.csr_out := csr.io.out


   io.imem.pc := ifu.io.mem_pc 
   io.imem.reset := reset
   ifu.io.mem_inst := io.imem.data 
   
   io.dmem <> lsu.io.dmem

}