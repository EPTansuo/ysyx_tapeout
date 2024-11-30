package cpu

import chisel3._
import chisel3.util._ 

import st_sel._
import ld_sel._


import state_m._
import state_s._

class LSU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_EXU_LSU(xlen)))
        val out = (Decoupled(new SigIO_LSU_WBU(xlen)))
        val dmem = Flipped(new DMemIO())
    })





    val in_reg = Reg(Output(chiselTypeOf(io.in)))
    val pc = in_reg.bits.pc
    val inst = in_reg.bits.inst
    val ctrlsig = in_reg.bits.lsu
    val src1 = in_reg.bits.src1
    val src2 = in_reg.bits.src2 
    val alu_out = in_reg.bits.alu_out
    val rd_addr = in_reg.bits.rd_addr
    val wbu_data = in_reg.bits.wbu
    val npc = in_reg.bits.npc

    // val fsm_s = Module(new ComFSM_S)
    // val state_s = fsm_s.io.state
    // fsm_s.io.valid := io.in.valid
    // fsm_s.io.ready := io.in.ready

    // when(state_s === read_s){
    //     in_reg := io.in
    // }
    // io.in.ready := state_s === read_s

    io.in.ready := io.in.valid
    when(io.in.ready){
        in_reg := io.in
    }


    val fsm_m = Module(new ComFSM_M)
    val state_m = fsm_m.io.state

    fsm_m.io.out_valid := io.out.valid
    fsm_m.io.out_ready := io.out.ready
    fsm_m.io.in_valid := io.in.valid
    fsm_m.io.in_ready := io.in.ready

    io.out.valid := state_m === wait_ready_m || state_m === write_m


     
    val st_data = MuxLookup(ctrlsig.st_sel, default = 0.U(xlen.W), Array(
        ST_XX -> 0.U(xlen.W),
        ST_SB -> src2(7, 0),
        ST_SH -> src2(15, 0),
        ST_SW -> src2
        )
    )

    
    val ld_data = MuxLookup(ctrlsig.ld_sel, default = 0.U(xlen.W), Array(
        LD_XX -> 0.U(xlen.W),
        LD_LB -> Cat(Fill(xlen-8, io.dmem.rdata(7)), io.dmem.rdata(7, 0)),
        LD_LH -> Cat(Fill(xlen-16, io.dmem.rdata(15)), io.dmem.rdata(15, 0)),
        LD_LW -> io.dmem.rdata,
        LD_LBU -> io.dmem.rdata(7, 0).asUInt,
        LD_LHU -> io.dmem.rdata(15, 0).asUInt
        )
    )

    io.dmem.reset := reset 
    io.dmem.clock := clock
    io.dmem.raddr := alu_out 
    io.dmem.we := ctrlsig.st_sel =/= ST_XX
    io.dmem.waddr := alu_out
    io.dmem.wdata := st_data
    io.dmem.wmask := MuxLookup(ctrlsig.st_sel, default = 0.U(4.W), Array(
        ST_XX -> 0.U(4.W),
        ST_SB -> "b0001".U,
        ST_SH -> "b0011".U,
        ST_SW -> "b1111".U
        )
    )




    io.out.bits.inst := inst
    io.out.bits.pc := pc
    io.out.bits.alu_out := alu_out
    io.out.bits.rd_addr := rd_addr
    io.out.bits.ld_data := ld_data
    io.out.bits.src1 := src1
    io.out.bits.wbu <> wbu_data
    io.out.bits.npc := npc 
    


}