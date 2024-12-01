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




class EXU(xlen: Int) extends Module{
    var io = IO(new Bundle{
        val in = Flipped(Decoupled(new SigIO_IDU_EXU(xlen)))
        val out = (Decoupled(new SigIO_EXU_LSU(xlen)))
        val reg_read1 = Flipped(new RegfileReadIO(xlen))
        val reg_read2 = Flipped(new RegfileReadIO(xlen))
        val csr_pc = Input(UInt(xlen.W))
        val csr_inst = Output(UInt(32.W))
    })

    val alu = Module(new ALU(xlen))
    val immGen = Module(new ImmGen(xlen))

    val in_reg = Reg(Output(chiselTypeOf(io.in)))
    val pc = in_reg.bits.pc
    val inst = in_reg.bits.inst
    val ctrlsig = in_reg.bits.exu
    val sig_csr_cmd = in_reg.bits.wbu.csr_cmd

    val s_idle :: s_wait_ready :: Nil = Enum(2)

    val state = RegInit(s_idle)         
    state := MuxLookup(state, s_idle, Seq(
        s_idle -> Mux(io.in.valid, s_wait_ready, s_idle),
        s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))


    io.out.valid := state === s_wait_ready
    io.in.ready := state === s_idle

    when( io.in.valid && io.in.ready){
        in_reg := io.in
    }

 

    // regfile
    val rd_addr = inst(11, 7)
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    
    io.reg_read1.addr := Mux(sig_csr_cmd === csr_cmd.CSR_P, 15.U,rs1_addr)
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
    io.out.bits.npc := npc
    
    io.csr_inst := io.in.bits.inst 

    io.out.bits.rd_addr := rd_addr
    io.out.bits.src1 := src1
    io.out.bits.src2 := src2
    io.out.bits.alu_out := alu.io.out
    io.out.bits.pc := pc
    io.out.bits.inst := inst
    io.out.bits.wbu <> in_reg.bits.wbu
    io.out.bits.lsu <> in_reg.bits.lsu


}