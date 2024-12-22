package cpu 

import chisel3._ 
import chisel3.util._
import freechips.rocketchip.amba.axi4._

//blockSize: Byte
case class CacheParameters(nSets: Int, nWays: Int, blockSize: Int, addrBits: Int, cntBits: Int) {
  val cacheSize: Int = nSets * nWays * blockSize
  val offsetBits: Int = (math.log(blockSize) / math.log(2)).toInt
  val indexBits: Int = (math.log(nSets) / math.log(2)).toInt
  val tagBits: Int = (addrBits - indexBits - offsetBits)
  override def toString: String = 
    s"Cache Size: $cacheSize bytes, Tag: $tagBits bits, Index: $indexBits bits, Offset: $offsetBits bits"
}


object ICacheParameters{
    def apply() = CacheParameters(
        nSets = 64,   // should be 2^n
        nWays = 4,   
        blockSize = 4, // should be 4*n  // only support 4 now 
        addrBits = 32,
        cntBits = 8,
    )
}

class ICacheIO(axiparams: AXI4BundleParameters) extends Bundle {
  val ifu = Flipped(new AXI4Bundle(axiparams))
  val imem = (new AXI4Bundle(axiparams))
}

// class CacheEntry(tagBits: Int, blockSize: Int, cntBits: Int) extends Bundle {
//   val valid = Bool()
//   val tag = UInt(tagBits.W)
//   val data = Vec(blockSize, UInt(8.W))
// //  val cnt = UInt(cntBits.W)
// }

class ICache(cacheparams: CacheParameters, axiparams: AXI4BundleParameters) extends Module{
    val io = IO(new ICacheIO(axiparams))

    val tagBits = cacheparams.tagBits
    val indexBits = cacheparams.indexBits
    val offsetBits = cacheparams.offsetBits
    val nSets = cacheparams.nSets
    val nWays = cacheparams.nWays
    val blockSize = cacheparams.blockSize
    val cntBits = cacheparams.cntBits
    
    assert(blockSize % (axiparams.dataBits/8) == 0, "iCache blockSize*8 must be N times of databits"); 


    //val cache = SyncReadMem(nWays, Vec(nSets, new CacheEntry(tagBits, blockSize, cntBits)))
    val totalLines = nWays * nSets 
    val cache_data = SyncReadMem(totalLines, UInt((blockSize*8).W))
    val cache_tag = SyncReadMem(totalLines, UInt(tagBits.W))
    val cache_valid = SyncReadMem(totalLines, UInt(1.W))


    val raddr_ifu = io.ifu.ar.bits.addr
    val rtag = raddr_ifu(tagBits + indexBits + offsetBits - 1, indexBits + offsetBits)
    val ridx = raddr_ifu(indexBits + offsetBits - 1, offsetBits)
    val roffset = raddr_ifu(offsetBits - 1, 0)


     val data_way = Wire(Vec(nWays, UInt((8*blockSize).W)))
     val hit_way = Wire(Vec(nWays, Bool()))
    // for (i <- 0 until nWays){
    //     when(cache(i)(ridx).tag === rtag){
    //         data_way(i) := cache(i)(ridx).data.asUInt
    //     }.otherwise{
    //         data_way(i) := 0.U
    //     }
    // }
    
    for (i <- 0 until nWays){
        // Can be optimized !!!!!!  乘法！！
        when(cache_tag(ridx*nWays.U+i.U) === rtag){
            data_way(i) := cache_data(ridx*nWays.U+i.U)
            hit_way(i) :=  cache_valid(ridx*nWays.U+i.U)
        }.otherwise{
            data_way(i) := 0.U 
            hit_way(i) :=  0.U
        }
    }

    val hit = hit_way.reduce(_ || _)


    val rdata_cache = Wire(UInt(32.W))
    if(blockSize == 4){
        rdata_cache := Mux1H(hit_way, data_way.map(dw => dw(31, 0)))
    }else{
        rdata_cache := Mux1H(hit_way, data_way.map(dw => (dw>>roffset)(31,0)))
    }
    

    // IF the address is not in the range of the SDRAM address, then it is a bypass
    val bypass = (io.ifu.ar.bits.addr(31,29) =/= "b101".U)


    val s_idle :: s_read :: s_replace :: s_refill :: s_bypass :: Nil = Enum(5)

    val state = RegInit(s_idle)
    val state_next = Wire(UInt(state.getWidth.W))
    state_next := MuxLookup(state, s_idle)(Seq(
        s_idle -> Mux(io.ifu.r.valid, Mux(bypass, s_bypass, s_read), s_idle),
        s_read -> Mux(hit, s_idle, s_replace),
        s_replace -> Mux(io.imem.ar.valid, s_refill, s_replace),
        s_refill -> Mux(io.imem.r.valid, s_idle, s_refill),
        s_bypass -> Mux(io.ifu.r.ready, s_idle, s_bypass)
    ))
    state := state_next

    io.ifu.ar.ready := state === s_idle
    io.ifu.r.valid := hit || (state === s_bypass && io.imem.r.valid)
    io.ifu.r.bits.data := Mux(state === bypass, io.imem.r.bits.data, rdata_cache)



    io.imem.ar.bits.addr := io.ifu.ar.bits.addr
    io.imem.ar.valid := Mux(bypass, io.ifu.ar.valid, state === s_replace)
    io.imem.r.ready := (state === s_refill) || (state === s_bypass && io.ifu.r.valid)


    val cache_refill = (state === s_refill && io.imem.r.valid)

    //  Refill
    // for (i <- 0 until nWays){
    //     for( j <- 0 until nSets){
    //         when(state === s_idle && state_next =/= s_idle && !cache_refill){

    //             // the cnt will not overflow
    //             when(cache(i)(j).cnt =/= (math.pow(2,cntBits)-1).toInt.U){
    //                 cache(i)(j).cnt := cache(i)(j).cnt +1.U;
    //             }
    //         }
    //     }
    // }


    io.imem.r.ready := state === s_refill || (state === s_bypass && io.ifu.r.ready)
    
    val wtag = Wire(UInt(rtag.getWidth.W))
    val widx = Wire(UInt(ridx.getWidth.W))
    val woffset = Wire(UInt(roffset.getWidth.W))
    widx := ridx
    woffset := roffset
    wtag := rtag

    // FIFO Ptr
    val fifoPtr = RegInit(VecInit(Seq.fill(nSets)(0.U(log2Ceil(nWays).W))))

    val victimWay = fifoPtr(widx)


    // val cnt_way = Wire(Vec(nWays, UInt((cntBits).W)))
    // for(i <- 0 until nWays){
    //     cnt_way(i) :=  cache(i)(widx).cnt
    // }

    // // Find the index of the maximum cnt
    // val wayChoice = Wire(UInt(log2Ceil(nWays).W))
    // wayChoice := 0.U
    // for (i <- 1 until nWays) {
    //     when(cnt_way(i) > cnt_way(wayChoice)) {
    //     wayChoice := i.U
    //     }
    // }

    // val maxCnt = cnt_way.reduce((a, b) => Mux(a > b, a, b))
    // // 再根据谁等于 maxCnt 来拿到下标
    // val indices = (0 until nWays).map(_.U)
    // val wayChoiceWire = PriorityMux(
    //     cnt_way.zip(indices).map{ case (cntVal, idx) => (cntVal === maxCnt, idx) }
    // )

    // // wayChoiceWire 就是组合逻辑输出，最后赋给 wayChoice
    // val wayChoice = Wire(UInt(log2Ceil(nWays).W))
    // wayChoice := wayChoiceWire

// io.max_value := MuxCase(0.U, io.cnt_way.map(elem => (elem === io.cnt_way.reduce((a, b) => Mux(a > b, a, b)), elem)))
   
   
    when(cache_refill){
            //cache(victimWay)(widx).data(i+offsetBits>>offsetBits) := io.imem.r.bits.data 
        cache_data(widx*nWays.U+victimWay) := io.imem.r.bits.data 
        assert(blockSize == 4);
        //cache(victimWay)(widx).tag := wtag 
        cache_tag(victimWay + widx*nWays.U) := wtag

        val nextWay = victimWay + 1.U
        fifoPtr(ridx) := Mux(nextWay === nWays.U, 0.U, nextWay)   // Can be optimized !!!!!!!!
    }


    io.ifu.r.bits.last := true.B   //  TODO ---- 
    io.ifu.r.bits.id := 0.U
    io.ifu.r.bits.resp := 0.U

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

}