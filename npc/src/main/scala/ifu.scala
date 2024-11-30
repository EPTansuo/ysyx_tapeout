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
    val mem_pc = Output(UInt(xlen.W))
    val mem_inst = Input(UInt(32.W))
  })


  val pc = RegInit(PC_INIT)


  // val fsm_s = Module(new ComFSM_S)
  // val state_s = fsm_s.io.state
  // fsm_s.io.valid := io.in.valid
  // fsm_s.io.ready := io.in.ready

  // io.in.ready := state_s === read_s

  io.in.ready := io.in.valid
  when(io.in.ready){
    pc := io.in.bits.npc
  }

  //io.out.valid := 1.U
  val fsm_m = Module(new ComFSM_M)
  val state_m = fsm_m.io.state
  fsm_m.io.out_valid := io.out.valid
  fsm_m.io.out_ready := io.out.ready
  fsm_m.io.in_valid := io.in.valid 
  fsm_m.io.in_ready := io.in.ready

  io.out.valid := state_m === idle_m || state_m === wait_ready_m 

  

  io.mem_pc := pc

  io.out.bits.pc := pc
  io.out.bits.inst := io.mem_inst

}
