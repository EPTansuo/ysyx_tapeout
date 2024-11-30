package cpu

import chisel3._
import chisel3.util._
import defines._ 
import state_m._
import state_s._

class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle { 
  //  val in = Flipped(Decoupled(new SIGIO_WBU_IFU(xlen)))
    val out = (Decoupled(new SigIO_IFU_IDU(xlen)))
    val pc_in = Input(UInt(xlen.W))
    val mem_pc = Output(UInt(xlen.W))
    val mem_inst = Input(UInt(32.W))
  })

  val pc = Reg(UInt(xlen.W))
  
  val fsm_m = Module(new ComFSM_M)
  val state_m = fsm_m.io.state
  fsm_m.io.valid := io.out.valid
  fsm_m.io.ready := io.out.ready

  when(state_m === idle_m){
    pc := io.pc_in
    io.out.valid := 1.U
  }.otherwise{
    io.out.valid := 0.U
  }


  io.mem_pc := pc
  io.out.bits.pc := pc
  io.out.bits.inst := io.mem_inst


}
