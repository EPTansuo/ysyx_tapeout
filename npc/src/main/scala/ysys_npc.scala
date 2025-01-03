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
                        stall:Bool, arch: String = "multi"): Unit = {
        arch match {
            case "multi"  =>   
                prevOut <> thisIn
            case "pipeline" =>
                prevOut.ready := thisIn.ready
                thisIn.bits := RegEnable(prevOut.bits, prevOut.valid && thisIn.ready || (stall&&thisIn.ready))
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

    val stall = Wire(Bool())
    
    val ifu = Module(new IFU(config))
    val idu = Module(new IDU(config))
    val exu = Module(new EXU(config))
    val lsu = Module(new LSU(config))
    val wbu = Module(new WBU(config))

    val stage_arch = "pipeline"
    ModuleConnect(wbu.io.out, ifu.io.in, ifu.io.out, false.B, stage_arch)
    ModuleConnect(ifu.io.out, idu.io.in, idu.io.out, false.B, stage_arch)
    ModuleConnect(idu.io.out, exu.io.in, exu.io.out, stall, stage_arch)
    ModuleConnect(exu.io.out, lsu.io.in, lsu.io.out, false.B, stage_arch)
    ModuleConnect(lsu.io.out, wbu.io.in, wbu.io.out, false.B, stage_arch)


    // Control Hazard
    val controlHazard = Module(new ControlHazard(config))
    controlHazard.io.ifu_pc <> ifu.io.pc
    controlHazard.io.idu_pc <> idu.io.pc
    controlHazard.io.exu_npc <> exu.io.npc
    ifu.io.flush := controlHazard.io.flush
    idu.io.flush := controlHazard.io.flush
    ifu.io.npc := exu.io.npc.bits 

    
    // Data Hazard
    def useRs(itype: UInt): Bool = {
        // useRS1 R,I,S,B,FENCE
        val useRs1 = itype === inst_type.R_TYPE || 
                     itype === inst_type.I_TYPE || 
                     itype === inst_type.S_TYPE || 
                     itype === inst_type.B_TYPE || 
                     itype === inst_type.FENCE_TYPE

        // useRS2 R,S,B
        val useRs2 = itype === inst_type.R_TYPE || 
                     itype === inst_type.S_TYPE || 
                     itype === inst_type.B_TYPE

        useRs1 || useRs2     
    }

    def conflictWithStage(rs1: UInt, rs2: UInt, rd: UInt, ID_inst_type: UInt, valid: Bool): Bool = {
         valid && ((rs1 === rd) || (rs2 === rd)) && rs1.orR && rs2.orR && useRs(ID_inst_type)
    }
    
    val IDU_rs1 = idu.io.out.bits.inst(19, 15)
    val IDU_rs2 = idu.io.out.bits.inst(24, 20)
    val IDU_inst_type = idu.io.inst_type
    val EXU_rd = exu.io.out.bits.rd_addr
    val LSU_rd = lsu.io.out.bits.rd_addr
    val WBU_rd = wbu.io.out.bits.rd_addr

    val exu_raw = Wire(Bool())
    val lsu_raw = Wire(Bool())
    val wbu_raw = Wire(Bool())

    exu_raw := conflictWithStage(IDU_rs1, IDU_rs2, EXU_rd, IDU_inst_type, ~exu.io.out.ready)
    lsu_raw := conflictWithStage(IDU_rs1, IDU_rs2, LSU_rd, IDU_inst_type, ~lsu.io.out.ready)
    wbu_raw := conflictWithStage(IDU_rs1, IDU_rs2, WBU_rd, IDU_inst_type, ~wbu.io.out.ready)

    // exu_raw := conflictWithStage(IDU_rs1, IDU_rs2, EXU_rd, IDU_inst_type, true.B)
    // lsu_raw := conflictWithStage(IDU_rs1, IDU_rs2, LSU_rd, IDU_inst_type, true.B)
    // wbu_raw := conflictWithStage(IDU_rs1, IDU_rs2, WBU_rd, IDU_inst_type, true.B)
    dontTouch(exu_raw)
    dontTouch(lsu_raw)
    dontTouch(wbu_raw)
    val isRAW = exu_raw || lsu_raw || wbu_raw
    // val isRAW = conflictWithStage(IDU_rs1, IDU_rs2, EXU_rd, IDU_inst_type, ~exu.io.out.valid) ||
    //                conflictWithStage(IDU_rs1, IDU_rs2, LSU_rd, IDU_inst_type, ~lsu.io.out.valid) ||
    //                conflictWithStage(IDU_rs1, IDU_rs2, WBU_rd, IDU_inst_type, ~wbu.io.out.valid)
    // val isRAW = false.B

    ifu.io.stall := isRAW
    idu.io.stall := isRAW
    stall := isRAW

    // Regfile
    val regfile = Module(new Regfile(config))
    exu.io.reg_read1 <> regfile.io.read1
    exu.io.reg_read2 <> regfile.io.read2
    wbu.io.reg_write <> regfile.io.write


    // ICache
    val icache = if(config.USE_ICACHE) Some(Module(new ICache(config))) else None
    icache.map { cache =>
        ifu.io.imem <> cache.io.ifu
        cache.io.fencei := ifu.io.fencei
    }

    // AXI Abriter
    val axi_arbiter = Module( new AXIArbiter(2, config.axiparams))
    axi_arbiter.io.in(0) <> icache.map(_.io.imem).getOrElse(ifu.io.imem)
    axi_arbiter.io.in(1) <> lsu.io.dmem
    axi_arbiter.io.out <> io.axi


}