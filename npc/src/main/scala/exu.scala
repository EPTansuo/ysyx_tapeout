package cpu 

import chisel3._
import chisel3.util._

import A_sel._
import B_sel._
import pc_sel._
import aluop._
import imm_sel._
import br_sel._
import csr_cmd._

import state_m._
import state_s._


class EXU(xlen: Int) extends Module{
    var io = IO(new Bundle{
        val in = Flipped(Decoupled(new SigIO_IDU_EXU(xlen)))
        val out = (Decoupled(new SigIO_EXU_LSU(xlen)))
        val reg_read1 = Flipped(new RegfileReadIO(xlen))
        val reg_read2 = Flipped(new RegfileReadIO(xlen))
        val npc = Output(UInt(xlen.W))
        val csr_pc = Input(UInt(xlen.W))
        val csr_inst = Output(UInt(32.W))
        val csr_cmd = Output(UInt(3.W))
    })

    val alu = Module(new ALU(xlen))
    val immGen = Module(new ImmGen(xlen))
    
    //val ctrlsig = io.in.bits.exu
    //val pc = io.in.bits.pc
    //val inst = io.in.bits.inst 
    val pc = Reg(UInt(xlen.W))
    val inst = Reg(UInt(32.W))
    val ctrlsig = io.in.bits.exu 

    val fsm_m = Module(new ComFSM_M)
    val state_m = fsm_m.io.state
    fsm_m.io.valid := io.out.valid
    fsm_m.io.ready := io.out.ready

    when(state_m === idle_m){
        pc := io.in.bits.pc
        inst := io.in.bits.inst
        io.out.valid := 1.U
    }.otherwise{
        io.out.valid := 0.U
    }

    val fsm_s = Module(new ComFSM_S)
    val state_s = fsm_s.io.state
    fsm_s.io.valid := io.in.valid
    fsm_s.io.ready := io.in.ready

    when(state_s === idle_s){
        io.in.ready := 1.U
    }.otherwise{
        io.in.ready := 1.U   //无需等待就能接收
    }







    // regfile
    val rd_addr = inst(11, 7)
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    io.reg_read1.addr := Mux(ctrlsig.csr_cmd === csr_cmd.CSR_P, 15.U,rs1_addr)
    io.reg_read2.addr := rs2_addr
    val src1 = io.reg_read1.data
    val src2 = io.reg_read2.data

    immGen.io.inst := inst 
    immGen.io.sel := ctrlsig.imm_sel

    
    alu.io.A := MuxLookup(ctrlsig.A_sel, default = 0.U(xlen.W), Array(
        A_RS1 -> src1,
        A_PC  -> pc
        )
    )

    alu.io.B := MuxLookup(ctrlsig.B_sel, default = 0.U(xlen.W), Array(
        B_RS2 -> src2,
        B_IMM -> immGen.io.out
        )
    )

    alu.io.aluop := ctrlsig.alu_op




    val branch = Module(new Branch(xlen))
    branch.io.br_sel := ctrlsig.br_sel
    branch.io.src1 := src1
    branch.io.src2 := src2

    val npc = MuxCase(
        pc + 4.U,  
        IndexedSeq(
        ((ctrlsig.pc_sel === PC_ALU) || (branch.io.taken)) -> (alu.io.out >> 1.U << 1.U),  //对齐
        (ctrlsig.pc_sel === PC_0) -> pc, 
        (ctrlsig.pc_sel === PC_CSR) -> io.csr_pc
        )
    )
    io.npc := npc 
    
    io.csr_inst := io.in.bits.inst 
    io.csr_cmd := ctrlsig.csr_cmd

    io.out.bits.rd_addr := rd_addr
    io.out.bits.src1 := src1
    io.out.bits.src2 := src2
    io.out.bits.alu_out := alu.io.out
    io.out.bits.pc := pc
    io.out.bits.inst := inst
    io.out.bits.wbu <> io.in.bits.wbu
    io.out.bits.lsu <> io.in.bits.lsu

    io.in.ready := 1.U

    io.out.valid := 1.U

}