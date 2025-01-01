package cpu

import chisel3._
import chisel3.util._
//import defines._ 

import AXI4._ 
import freechips.rocketchip.amba.axi4._


class IFU(config: NPCConfig) extends Module {
  val io = IO(new Bundle { 
    val in = Flipped(Decoupled(new SigIO_WBU_IFU(config.XLEN)))
    val out = (Decoupled(new SigIO_IFU_IDU(config.XLEN)))

    val npc = Input(UInt(config.XLEN.W))
    val fencei = Output(Bool())
    // val mem_pc = Output(UInt(xlen.W))
    // val mem_inst = Input(UInt(32.W))
    //val imem = new AXILiteMasterIF(addrWidthBits = 32, dataWidthBits = xlen)
    val imem = new AXI4Bundle(config.axiparams)
    val pc = Decoupled((UInt(config.XLEN.W)))
    val flush = Input(Bool())
  })

  val isFirst = RegInit(true.B)
  when(isFirst){
    isFirst := false.B
  }
  //val in_valid = Mux(isFirst, true.B, io.in.valid)
  val in_valid = true.B
  val in_ready = io.in.ready


  val s_idle :: s_read ::s_wait_read :: s_wait_ready :: Nil = Enum(4)
  val state = RegInit(s_idle)         
  state := MuxLookup(state, s_idle)(Seq(
    s_idle -> Mux(in_valid && io.out.ready, s_read, s_idle),
    s_read -> Mux(io.imem.ar.ready, s_wait_read, s_read),
    s_wait_read -> Mux(io.imem.r.valid, s_wait_ready, s_wait_read),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))

 


  io.out.valid := state === s_wait_ready
  io.in.ready := state === s_idle



  val pc = RegInit(config.PC_INIT.U)
  // when( io.in.valid && io.in.ready){
  //     pc := io.in.bits.npc
  // }
  // when(io.out.valid){
  //   pc := pc + 4.U
  // }
  // val bpu = Module(new BPU(config))
  // bpu.io.pc := pc 
  // bpu.io.wbu_npc := io.in.bits.npc
  // bpu.io.update := io.in.valid && state === s_idle

  // when(state === s_idle && io.in.valid && io.in.ready){
  //   pc := io.in.bits.npc
  // }
  when(io.out.ready && state === s_wait_ready){
    pc := pc+4.U// bpu.io.npc
  }
  
  when(io.flush){
    state := s_idle
    pc := io.npc
  }
  io.pc.valid := true.B 
  io.pc.bits := pc 

  io.imem.ar.valid := state === s_read
  io.imem.ar.bits.addr := pc
  io.imem.ar.bits.prot := 0.U
  io.imem.r.ready := true.B
  io.imem.ar.bits.id := 0.U
  io.imem.ar.bits.len := 0.U
  io.imem.ar.bits.size := 2.U
  io.imem.ar.bits.burst := 0.U
  io.imem.ar.bits.lock := 0.U
  io.imem.ar.bits.cache := 0.U
  io.imem.ar.bits.qos := 0.U


  val inst = RegInit(0.U(32.W))
  when(io.imem.r.valid && io.imem.r.ready){
    inst := io.imem.r.bits.data
  }

  when(io.imem.r.bits.data === insts.fencei){
    io.fencei := io.out.valid
  }.otherwise{
    io.fencei := false.B
  }
  dontTouch(io.fencei)
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
  io.imem.aw.bits.id := 0.U
  io.imem.aw.bits.len := 0.U
  io.imem.aw.bits.size := 2.U
  io.imem.aw.bits.burst := 0.U
  io.imem.aw.bits.lock := 0.U
  io.imem.aw.bits.cache := 0.U
  io.imem.aw.bits.qos := 0.U
  io.imem.w.bits.last := true.B


  if(config.PERF_CNT){
    val ifu_cnt = RegInit(0.U(32.W))
    when(io.in.valid && io.in.ready){
      ifu_cnt := ifu_cnt + 1.U
    }
    dontTouch(ifu_cnt)
  }
}
