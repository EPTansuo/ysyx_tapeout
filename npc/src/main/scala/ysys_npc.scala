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
                   flush: Bool,  arch: String = "multi"): Unit = {
        arch match {
            case "multi"  =>   
                prevOut <> thisIn
            case "pipeline" =>
                prevOut.ready := thisIn.ready
                thisIn.bits := RegEnable(prevOut.bits, prevOut.valid && thisIn.ready)
                thisIn.valid := RegEnable(prevOut.valid && ~flush, thisIn.ready || flush)
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

        // Control Hazard
    val controlHazard = Module(new ControlHazard(config))
    controlHazard.io.ifu_pc <> ifu.io.pc
    controlHazard.io.idu_pc <> idu.io.pc
    controlHazard.io.exu_npc <> exu.io.npc
    ifu.io.flush := controlHazard.io.flush
    idu.io.flush := controlHazard.io.flush
    ifu.io.exu_npc := exu.io.npc.bits 


    val stage_arch = "pipeline"
    ModuleConnect(wbu.io.out, ifu.io.in, ifu.io.out, false.B, stage_arch)
    ModuleConnect(ifu.io.out, idu.io.in, idu.io.out, controlHazard.io.flush, stage_arch)
    ModuleConnect(idu.io.out, exu.io.in, exu.io.out, controlHazard.io.flush, stage_arch)
    ModuleConnect(exu.io.out, lsu.io.in, lsu.io.out, false.B, stage_arch)
    ModuleConnect(lsu.io.out, wbu.io.in, wbu.io.out, false.B, stage_arch)




    
    // Data Hazard
    val stall = Wire(Bool())
    def useRs1(itype: UInt): Bool = {
        // useRS1 R,I,S,B,FENCE
        val useRs1 = itype === inst_type.R_TYPE || 
                     itype === inst_type.I_TYPE || 
                     itype === inst_type.S_TYPE || 
                     itype === inst_type.B_TYPE || 
                     itype === inst_type.FENCE_TYPE

        useRs1
    }
    def useRs2(itype: UInt): Bool = {
        // useRS2 R,S,B
        val useRs2 = itype === inst_type.R_TYPE || 
                     itype === inst_type.S_TYPE || 
                     itype === inst_type.B_TYPE

        useRs2
    }

    def writeReg(itype: UInt): Bool = {
        // writeReg R,I,S,B,U,J
        val writeReg = itype === inst_type.R_TYPE || 
                       itype === inst_type.I_TYPE || 
                       itype === inst_type.U_TYPE || 
                       itype === inst_type.J_TYPE ||
                       itype === inst_type.FENCE_TYPE

        writeReg
    }
    def conflictWithStage(rs1: UInt, rs2: UInt, rd: UInt, ID_inst_type: UInt, Other_inst_type: UInt): Bool = {
        ((rs1 === rd && useRs1(ID_inst_type)) || (rs2 === rd && useRs2(ID_inst_type) )) && (rd.orR) && writeReg(Other_inst_type)

    }
    
    val IDU_rs1 = idu.io.in.bits.inst(19, 15)
    val IDU_rs2 = idu.io.in.bits.inst(24, 20)
    val IDU_inst_type = idu.io.out.bits.wbu.inst_type
    val EXU_rd = exu.io.rd_addr
    val LSU_rd = lsu.io.rd_addr
    val WBU_rd = wbu.io.rd_addr
    val EXU_inst_type = exu.io.out.bits.wbu.inst_type
    val LSU_inst_type = lsu.io.out.bits.wbu.inst_type
    val WBU_inst_type = wbu.io.inst_type

    val exu_raw = Wire(Bool())
    val lsu_raw = Wire(Bool())
    val wbu_raw = Wire(Bool())
    exu_raw := conflictWithStage(IDU_rs1, IDU_rs2, EXU_rd, IDU_inst_type, EXU_inst_type)
    lsu_raw := conflictWithStage(IDU_rs1, IDU_rs2, LSU_rd, IDU_inst_type, LSU_inst_type)
    wbu_raw := conflictWithStage(IDU_rs1, IDU_rs2, WBU_rd, IDU_inst_type, WBU_inst_type)
    dontTouch(exu_raw)
    dontTouch(lsu_raw)
    dontTouch(wbu_raw)
    val isRAW = exu_raw || lsu_raw || wbu_raw


    val RAW_with_Fwd = Wire(Bool())
    RAW_with_Fwd := ( ( IDU_rs1 === EXU_rd && useRs1(IDU_inst_type) && IDU_rs1.orR ) ||
                      ( IDU_rs2 === EXU_rd && useRs2(IDU_inst_type) && IDU_rs2.orR ) ) &&
                      idu.io.out.bits.lsu.ld_sel =/= ld_sel.LD_XX
    dontTouch(RAW_with_Fwd)



    // load-use 
    def loadMem(ld_sel_ : UInt): Bool = {
        val ret = ld_sel_ =/= ld_sel.LD_XX
        ret
    }
    def conflictWithLoadUse(rs1: UInt, rs2: UInt, rd: UInt, ID_inst_type: UInt, Other_ld_sel: UInt): Bool = {
        ((rs1 === rd && useRs1(ID_inst_type)) || (rs2 === rd && useRs2(ID_inst_type) )) && (rd.orR) && loadMem(Other_ld_sel)

    }    
    val exu_raw_loaduse = conflictWithLoadUse(IDU_rs1, IDU_rs2, EXU_rd, IDU_inst_type, exu.io.out.bits.lsu.ld_sel)
    val lsu_raw_loaduse = conflictWithLoadUse(IDU_rs1, IDU_rs2, LSU_rd, IDU_inst_type, lsu.io.ld_sel)
    val isRAW_loaduse = exu_raw_loaduse || lsu_raw_loaduse
    
    dontTouch(exu_raw_loaduse)
    dontTouch(lsu_raw_loaduse)
    dontTouch(isRAW_loaduse)

    idu.io.stall := stall
    stall := RAW_with_Fwd || RegNext(RAW_with_Fwd) || isRAW_loaduse
    dontTouch(RAW_with_Fwd)
    
    if(config.PERF_CNT){
        val loaduse_cnt = RegInit(0.U(64.W))
        when(isRAW_loaduse){
            loaduse_cnt := loaduse_cnt + 1.U
        }
        dontTouch(loaduse_cnt)
    }
    


    // // forwarding 
    // def forwardFrom(IDU_rs1: UInt, rd: UInt, itype: UInt): UInt = {
    //     val ret = IDU_rs1 && (IDU_rs1 === rd) && RegWrite(itype)
    //     ret
    // }
    // val exu_forward = forwardFrom(IDU_rs1, EXU_rd, IDU_inst_type)
    // val lsu_forward = forwardFrom(IDU_rs1, LSU_rd, IDU_inst_type)
    // val wbu_forward = forwardFrom(IDU_rs1, WBU_rd, IDU_inst_type)
    
    // Forwarding 
    val forward = Module(new Forward(config))
    forward.io.idu_rs1 := IDU_rs1
    forward.io.idu_rs2 := IDU_rs2
    forward.io.exu_rd := EXU_rd
    forward.io.lsu_rd := LSU_rd
    forward.io.wbu_rd := WBU_rd
    forward.io.exu_itype := EXU_inst_type
    forward.io.lsu_itype := LSU_inst_type
    forward.io.wbu_itype := WBU_inst_type
    idu.io.forward_A := Mux(isRAW_loaduse, forward_sel.FWD_XX, forward.io.forward_A)
    idu.io.forward_B := Mux(isRAW_loaduse, forward_sel.FWD_XX, forward.io.forward_B)
    idu.io.forward_exu := exu.io.out.bits.alu_out
    idu.io.forward_lsu := lsu.io.out.bits.alu_out
    idu.io.forward_wbu := wbu.io.forward_wbu


    // Regfile
    val regfile = Module(new Regfile(config))
    // exu.io.reg_read1 <> regfile.io.read1
    // exu.io.reg_read2 <> regfile.io.read2
    idu.io.reg_read1 <> regfile.io.read1
    idu.io.reg_read2 <> regfile.io.read2
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