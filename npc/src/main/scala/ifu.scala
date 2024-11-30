package cpu

import chisel3._
import chisel3.util._
import defines._ 

class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle { 
    val out = (Decoupled(new SigIO_IFU_IDU(xlen)))
    val pc_in = Input(UInt(xlen.W))
    val mem_pc = Output(UInt(xlen.W))
    val mem_inst = Input(UInt(32.W))
  })

  io.mem_pc := io.pc_in

  io.out.bits.pc := io.pc_in
  io.out.bits.inst := io.mem_inst
  
  io.out.valid := 1.U

}
