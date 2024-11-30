package cpu

import chisel3._
import chisel3.util._

import state_m._
import state_s._

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

    val fsm_s = Module(new ComFSM_S)
    val state_s = fsm_s.io.state
    fsm_s.io.valid := io.in.valid
    fsm_s.io.ready := io.in.ready

    io.in.ready := state_s === read_s


    when(state_s === read_s){
        inst := io.in.bits.inst
        pc := io.in.bits.pc
    }

    val fsm_m = Module(new ComFSM_M)
    val state_m = fsm_m.io.state
    fsm_m.io.out_valid := io.out.valid
    fsm_m.io.out_ready := io.out.ready
    fsm_m.io.in_valid := io.in.valid 
    fsm_m.io.in_ready := io.in.ready

    io.out.valid := state_m === wait_ready_m || state_m === write_m || state_m === idle_m
    
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
    io.out.bits.exu.csr_cmd := control.io.out.csr_cmd
    io.out.bits.exu.br_sel := control.io.out.br_sel
    io.out.bits.exu.pc_sel := control.io.out.pc_sel



}