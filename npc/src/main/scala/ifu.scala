package cpu

import chisel3._
import chisel3.util._
import defines._ 

class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle { 
    val inst_out = Decoupled(UInt(32.W))
    val pc_in = Input(UInt(xlen.W))
    val pc_out = Output(UInt(xlen.W))
    val mem_pc = Output(UInt(xlen.W))
    val mem_inst = Input(UInt(32.W))
  })

  io.pc_out := io.pc_in
  io.mem_pc := io.pc_out
  io.inst_out.bits := io.mem_inst

  io.inst_out.valid := 1.U   //目前没有实现，先写死
}
