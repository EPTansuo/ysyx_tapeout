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

    val exu_pc_sig = Flipped(Valid(new SigIO_EXU_IFU(config.XLEN)))
    val idu_pc_sig = Input(UInt(config.XLEN.W))

    val fencei = Output(Bool())

    val imem = new AXI4Bundle(config.axiparams)
    val pc = Valid((UInt(config.XLEN.W)))
    val flush = Input(Bool())
    // val stall = Input(Bool())
  })

  // val stall = io.stall
  val isFirst = RegInit(true.B)
  when(isFirst){
    isFirst := false.B
  }
  //val in_valid = Mux(isFirst, true.B, io.in.valid)
  val in_valid = true.B
  val in_ready = io.in.ready
  val inst = RegInit(0.U(32.W))
  val flush = RegInit(false.B)
 

  val s_idle :: s_read ::s_wait_read :: s_wait_ready :: Nil = Enum(4)
  val state = RegInit(s_idle)         
  state := MuxLookup(state, s_idle)(Seq(
    s_idle -> Mux(in_valid && io.out.ready, s_read, s_idle),
    s_read -> Mux(io.imem.ar.ready, s_wait_read, s_read),
    s_wait_read -> Mux(io.imem.r.valid, s_wait_ready, s_wait_read),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))

 when(io.flush){
    flush := true.B
  }.elsewhen(state === s_idle){
    flush := false.B
  }


  io.out.valid := state === s_wait_ready && ~(flush || io.flush)
  // io.in.ready := state === s_idle && ~stall
  io.in.ready := true.B


  val pc = RegInit(config.PC_INIT.U)

  val bpu_params = config.bpuparameters
  if(config.USE_BPU){
    if(bpu_params.useDynamic){ // USE BPU
      val bpu = Module(new BPU(config))
      bpu.io.pc := pc 
      bpu.io.exu_npc.bits := io.exu_pc_sig.bits.npc
      bpu.io.exu_npc.valid := io.exu_pc_sig.valid
      bpu.io.exu_pc  := io.exu_pc_sig.bits.pc
      bpu.io.update := io.flush
      bpu.io.idu_pc := io.idu_pc_sig
      when(io.out.valid && io.out.ready && ~flush) {
        pc := bpu.io.npc
      }
    }else{
      val immB = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W)).asSInt
      val immBExtend = immB.pad(config.XLEN).asUInt
      when(io.out.valid && io.out.ready && ~flush){
        when(inst(6,0) === "b1100011".U && inst(31) === 1.U){ //taken if pc decrease
          pc := pc + Mux(inst(31), immBExtend, 4.U);
        }.otherwise{
          pc :=  pc + 4.U
        }
      }
      
    }
  }else{ // Do not use BPU 
    when(io.out.valid && io.out.ready && ~flush) {
      pc := pc + 4.U
    }
  }
  


  when(flush){
    //state := s_idle
    pc := io.exu_pc_sig.bits.npc
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
    val ifu_cnt = RegInit(0.U(64.W))
    val flush_cnt = RegInit(0.U(64.W))
    val branch_cnt = RegInit(0.U(64.W))
    val jmp_cnt = RegInit(0.U(64.W))
    val branch_predict_failed_cnt = RegInit(0.U(64.W))
    when(io.in.valid && io.in.ready){
      ifu_cnt := ifu_cnt + 1.U
    }
    when(io.flush || flush){
      flush_cnt := flush_cnt + 1.U
    }
    when(io.out.valid && io.out.ready && inst(6,0) === "b1100011".U){
      branch_cnt := branch_cnt + 1.U
    }
    when( io.flush ){
      // also include jump predict failure
      branch_predict_failed_cnt := branch_predict_failed_cnt + 1.U
    }
    dontTouch(ifu_cnt)
    dontTouch(flush_cnt)
    dontTouch(branch_cnt)
    dontTouch(branch_predict_failed_cnt)
  }
}
