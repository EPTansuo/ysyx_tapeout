package cpu 

import chisel3._ 
import chisel3.util._
import freechips.rocketchip.amba.axi4._


class ICacheIO(axiparams: AXI4BundleParameters) extends Bundle {
  val ifu = Flipped(new AXI4Bundle(axiparams))
  val imem = (new AXI4Bundle(axiparams))
}



class ICache(config: NPCConfig) extends Module{
    val io = IO(new ICacheIO(config.axiparams))
    val cacheparams = config.icacheparams
    val tagBits = cacheparams.tagBits
    val indexBits = cacheparams.indexBits
    val offsetBits = cacheparams.offsetBits
    val nSets = cacheparams.nSets
    val nWays = cacheparams.nWays
    val blockSize = cacheparams.blockSize
    
    assert(blockSize % (config.axiparams.dataBits/8) == 0, "iCache blockSize*8 must be N times of databits"); 


    val totalLines = nWays * nSets 
    val cache_data = SyncReadMem(totalLines, Vec(blockSize/4, UInt(32.W)))
    val cache_tag = SyncReadMem(totalLines, UInt(tagBits.W))
    val cache_valid = SyncReadMem(totalLines, UInt(1.W))


    val raddr_ifu = io.ifu.ar.bits.addr
    val rtag = raddr_ifu(tagBits + indexBits + offsetBits - 1, indexBits + offsetBits)
    val ridx = raddr_ifu(indexBits + offsetBits - 1, offsetBits)
    val roffset = raddr_ifu(offsetBits - 1, 0)


     val data_way = Wire(Vec(nWays, Vec(blockSize/4, UInt(32.W))))
     val hit_way = Wire(Vec(nWays, Bool()))

    for (i <- 0 until nWays){ 
        // Can be optimized !!!!!!  乘法！！
        when(cache_tag(ridx*nWays.U+i.U) === rtag){
            data_way(i) := cache_data(ridx*nWays.U+i.U)
            hit_way(i) :=  cache_valid(ridx*nWays.U+i.U)
        }.otherwise{
            data_way(i) := VecInit(Seq.fill(blockSize/4)(0.U(32.W)))
            hit_way(i) :=  0.U
        }
    }

    val hit = Wire(Bool())
    hit := hit_way.reduce(_ || _)
    dontTouch(hit)
    
    val blockdata = Wire(Vec(blockSize/4, UInt(32.W)))
    blockdata := Mux1H(hit_way, data_way)


    val rdata_cache = Wire(UInt(32.W))
    if(blockSize == 4){
        rdata_cache := blockdata(0)
    }else {
        rdata_cache := blockdata(roffset(roffset.getWidth-1,2))
    }
    

    
    val bypass = Wire(UInt(1.W))
    if(config.USE_SOC){
        // IF the address is not in the range of the SDRAM address, then it is a bypass
        bypass := (io.ifu.ar.bits.addr(31,29) =/= "b101".U)
    }else{
        bypass := (io.ifu.ar.bits.addr(31,28) =/= "b1000".U)
    }
    //bypass := 1.U 
    val s_idle :: s_read :: s_replace :: s_refill :: Nil = Enum(4)

    val state = RegInit(s_idle)
    val state_next = Wire(UInt(state.getWidth.W))
    state_next := MuxLookup(state, s_idle)(Seq(
        s_idle -> Mux(io.ifu.ar.valid, Mux(bypass.asBool, s_idle, s_read), s_idle),
        s_read -> Mux(hit, s_idle, s_replace),
        s_replace -> Mux(io.imem.ar.ready, s_refill, s_replace),
        s_refill -> Mux(io.imem.r.valid, s_read, s_refill)
    ))
    state := state_next

    io.ifu.ar.ready := Mux(bypass.asBool , io.imem.ar.ready, state === s_idle)
    io.ifu.r.valid := Mux(bypass.asBool, io.imem.r.valid, (state === s_read && hit) )
    io.ifu.r.bits.data := Mux(bypass.asBool, io.imem.r.bits.data, rdata_cache)
    dontTouch(io.ifu.r.valid)

    io.imem.ar.bits.addr := io.ifu.ar.bits.addr
    io.imem.ar.valid := Mux(bypass.asBool, io.ifu.ar.valid, (state === s_replace) || (state === s_refill))
    io.imem.r.ready := Mux(bypass.asBool, io.ifu.r.valid ,state === s_refill)


    val cache_refill = (state === s_refill && io.imem.r.valid)


    val wtag = Wire(UInt(rtag.getWidth.W))
    val widx = Wire(UInt(ridx.getWidth.W))
    val woffset = Wire(UInt(roffset.getWidth.W))
    widx := ridx
    dontTouch(widx)
    woffset := roffset
    wtag := rtag

    // FIFO Ptr
    val fifoPtr = RegInit(VecInit(Seq.fill(nSets)(0.U(log2Ceil(nWays).W))))

    val victimWay = fifoPtr(widx)


   val cache_refill_prev = RegInit(false.B)
   cache_refill_prev := cache_refill
    when(!cache_refill_prev & cache_refill){
        cache_data(widx*nWays.U+victimWay)(0) := io.imem.r.bits.data 
        //assert(blockSize == 4);
        cache_tag(victimWay + widx*nWays.U) := wtag
        cache_valid(victimWay + widx*nWays.U) := 1.U

        val nextWay = victimWay + 1.U
        fifoPtr(ridx) := Mux(nextWay === nWays.U, 0.U, nextWay)   // Can be optimized !!!!!!!!
    }


    io.ifu.r.bits.last := Mux(bypass.asBool,  io.imem.r.bits.last , true.B)//  TODO ---- 
    io.ifu.r.bits.id := Mux(bypass.asBool,  io.imem.r.bits.last , 0.U)
    io.ifu.r.bits.resp := Mux(bypass.asBool, io.imem.r.bits.resp, 0.U)



    io.imem.ar.bits.prot := 0.U
    io.imem.ar.bits.id := 0.U
    io.imem.ar.bits.len := 0.U
    io.imem.ar.bits.size := 2.U
    io.imem.ar.bits.burst := 0.U
    io.imem.ar.bits.lock := 0.U
    io.imem.ar.bits.cache := 0.U
    io.imem.ar.bits.qos := 0.U
    

 //Icache Dont need to write 
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


    io.ifu.aw.ready := false.B
    io.ifu.w.ready := false.B
    io.ifu.b.valid := false.B
    io.ifu.b.bits.id := 0.U
    io.ifu.b.bits.resp := 0.U


    if(config.PERF_CNT){
        val icache_access_cnt = RegInit(0.U(64.W))
        val icache_hit_cnt = RegInit(0.U(64.W))
        val state_delay = RegInit(0.U(state.getWidth.W))
        val icache_bypass_cnt = RegInit(0.U(64.W))
        val icache_hit_access_time_cnt = RegInit(0.U(64.W))
        val icache_miss_penalty_cnt = RegInit(0.U(64.W))
        dontTouch(icache_access_cnt)
        dontTouch(icache_hit_cnt)
        dontTouch(state_delay)
        dontTouch(icache_bypass_cnt)
        dontTouch(icache_hit_access_time_cnt)
        dontTouch(icache_miss_penalty_cnt)
        state_delay := state 
        when(state === s_idle && state_next =/= s_idle){
            icache_access_cnt := icache_access_cnt + 1.U
        }
        when(state_delay === s_idle && state === s_read && hit){
            icache_hit_cnt := icache_hit_cnt + 1.U
        }
        when(io.ifu.r.valid && io.ifu.r.valid && bypass.asBool){
            icache_bypass_cnt := icache_bypass_cnt + 1.U 
        }
        when(state === s_read){
            icache_hit_access_time_cnt := icache_hit_access_time_cnt + 1.U
        }
        when(state === s_refill || state === s_replace){
            icache_miss_penalty_cnt := icache_miss_penalty_cnt + 1.U
        }
    }

}