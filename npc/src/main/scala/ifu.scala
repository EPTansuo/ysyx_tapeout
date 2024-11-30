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

  val isFirst = RegInit(true.B)
  when(isFirst){
    isFirst := false.B
  }
  val in_valid = Mux(isFirst, true.B, io.in.valid)
  val in_ready = io.in.ready


  val s_idle :: s_wait_ready :: Nil = Enum(2)

  val state = RegInit(s_idle)         
  state := MuxLookup(state, s_idle, Seq(
    s_idle -> Mux(in_valid, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))

  
  io.out.valid := state === s_wait_ready
  io.in.ready := state === s_idle

  val pc = RegInit(PC_INIT)
  when( io.in.valid && io.in.ready){
      pc := io.in.bits.npc
  }


  io.mem_pc := pc

  io.out.bits.pc := pc
  io.out.bits.inst := io.mem_inst

}
