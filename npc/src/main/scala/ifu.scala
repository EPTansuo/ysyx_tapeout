package cpu

import chisel3._
import chisel3.util._
import defines._ 

import state_m._
import state_s._

class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle { 
    val in = Flipped(Decoupled(new SigIO_WBU_IFU(xlen)))
    val out = (Decoupled(new SigIO_IFU_IDU(xlen)))
    val pc_in = Input(UInt(xlen.W))
    val mem_pc = Output(UInt(xlen.W))
    val mem_inst = Input(UInt(32.W))
  })

  io.mem_pc := io.pc_in

  io.out.bits.pc := io.pc_in
  io.out.bits.inst := io.mem_inst
  
  val fsm_s = Module(new ComFSM_S)
  val state_s = fsm_s.io.state
  fsm_s.io.valid := io.in.valid
  fsm_s.io.ready := io.in.ready

  // when(state_s === read_s){
  //   in_reg := io.in
  // }
  io.in.ready := state_s === read_s

  //io.out.valid := 1.U
  val fsm_m = Module(new ComFSM_M)
  val state_m = fsm_m.io.state
  fsm_m.io.out_valid := io.out.valid
  fsm_m.io.out_ready := io.out.ready
  fsm_m.io.in_valid := io.in.valid 
  fsm_m.io.in_ready := io.in.ready

  io.out.valid := state_m === wait_ready_m || state_m === write_m



}
