package cpu

import chisel3._
import chisel3.util._
import defines._ 

import AXI4._ 


class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle { 
    val in = Flipped(Decoupled(new SigIO_WBU_IFU(xlen)))
    val out = (Decoupled(new SigIO_IFU_IDU(xlen)))
    // val mem_pc = Output(UInt(xlen.W))
    // val mem_inst = Input(UInt(32.W))
    val imem = new AXILiteMasterIF(addrWidthBits = 32, dataWidthBits = xlen)
  })

  val isFirst = RegInit(true.B)
  when(isFirst){
    isFirst := false.B
  }
  val in_valid = Mux(isFirst, true.B, io.in.valid)
  val in_ready = io.in.ready


  val s_idle :: s_read ::s_wait_read :: s_wait_ready :: Nil = Enum(4)

  val state = RegInit(s_idle)         
  state := MuxLookup(state, s_idle)(Seq(
    s_idle -> Mux(in_valid, s_read, s_idle),
    s_read -> Mux(io.imem.ar.ready, s_wait_read, s_read),
    s_wait_read -> Mux(io.imem.r.valid, s_wait_ready, s_wait_read),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))

  
  io.out.valid := state === s_wait_ready
  io.in.ready := state === s_idle

  val pc = RegInit(PC_INIT)
  when( io.in.valid && io.in.ready){
      pc := io.in.bits.npc
  }


  io.imem.ar.valid := state === s_read
  io.imem.ar.bits.addr := pc
  io.imem.ar.bits.prot := 0.U
  io.imem.r.ready := true.B


  val inst = RegInit(0.U(32.W))
  when(io.imem.r.valid && io.imem.r.ready){
    inst := io.imem.r.bits.data
  }

  io.out.bits.inst := inst
  io.out.bits.pc := pc


  // 不需要写
  io.imem.w.valid := false.B
  io.imem.aw.valid := false.B
  io.imem.aw.bits.addr := 0.U
  io.imem.aw.bits.prot := 0.U
  io.imem.w.bits.data := 0.U
  io.imem.w.bits.strb := 0.U
  io.imem.b.ready := false.B


}
