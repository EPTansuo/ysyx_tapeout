package cpu

import chisel3._
import chisel3.util._




import defines._

class IDU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_IFU_IDU(xlen)))
        //val out = Output(new ControlOut(xlen))
        val out = (Decoupled(new SigIO_IDU_EXU(xlen)))
    })


    val control = Module(new Control(xlen))
    //val inst = io.in.bits.inst 
    //val pc = io.in.bits.pc
    val inst = RegInit(0.U(32.W))
    val pc = RegInit(0.U(32.W))

    val s_idle :: s_wait_ready :: Nil = Enum(2)

    val state = RegInit(s_idle)         
    state := MuxLookup(state, s_idle, Seq(
        s_idle -> Mux(io.in.valid, s_wait_ready, s_idle),
        s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))


    io.out.valid := state === s_wait_ready
    io.in.ready := state === s_idle

    when( io.in.valid && io.in.ready){
        inst := io.in.bits.inst
        pc := io.in.bits.pc
    }

    control.io.in.inst := inst 
    control.io.in.pc := pc 

    io.out.bits.inst := inst
    io.out.bits.pc := pc
    io.out.bits.exu.A_sel := control.io.out.A_sel
    io.out.bits.exu.B_sel := control.io.out.B_sel
    io.out.bits.exu.alu_op := control.io.out.alu_op
    io.out.bits.exu.imm_sel := control.io.out.imm_sel
    io.out.bits.lsu.ld_sel := control.io.out.ld_sel
    io.out.bits.lsu.st_sel := control.io.out.st_sel
    io.out.bits.wbu.wb_sel := control.io.out.wb_sel
    io.out.bits.wbu.csr_cmd := control.io.out.csr_cmd
    io.out.bits.exu.br_sel := control.io.out.br_sel
    io.out.bits.exu.pc_sel := control.io.out.pc_sel



}