package cpu 

import chisel3._ 
import chisel3.util._
import freechips.rocketchip.amba.axi4._


case class CacheParameters(nSets: Int, nWays: Int, rowBytes: Int, xlen: Int) {
  val cacheSize: Int = nSets * nWays * rowBytes
  val offsetBits: Int = (math.log(rowBytes) / math.log(2)).toInt
  val indexBits: Int = (math.log(nSets) / math.log(2)).toInt
  val tagBits: Int = (xlen - indexBits - offsetBits)
  override def toString: String = 
    s"Cache Size: $cacheSize bytes, Tag: $tagBits bits, Index: $indexBits bits, Offset: $offsetBits bits"
}


object ICacheParameters{
    def apply() = CacheParameters(
        nSets = 64,
        nWays = 4,
        rowBytes = 4,
        xlen = 32
    )
}

class ICacheIO(axiparams: AXI4BundleParameters) extends Bundle {
  val ifu = Flipped(new AXI4Bundle(axiparams))
  val imem = (new AXI4Bundle(axiparams))
}

class CacheEntry(tagBits: Int, rowBytes: Int) extends Bundle {
  val valid = Bool()
  val tag = UInt(tagBits.W)
  val data = Vec(rowBytes, UInt(8.W))
}

class ICache(cacheparams: CacheParameters, axiparams: AXI4BundleParameters) extends Module{
    val io = IO(new ICacheIO(axiparams))

    val tagBits = cacheparams.tagBits
    val indexBits = cacheparams.indexBits
    val offsetBits = cacheparams.offsetBits
    val nSets = cacheparams.nSets
    val nWays = cacheparams.nWays
    val rowBytes = cacheparams.rowBytes
    
    // var cache = for(i <- 0 until nWays) yield {
    //     for(j <- 0 until nSets) yield {
    //         val valid = RegInit(false.B)
    //         val tag = RegInit(0.U(tagBits.W))
    //         val data = Reg(Vec(rowBytes, UInt(8.W)))
    //         (valid, tag, data)
    //     }
    // }
    val cache = SyncReadMem(nWays, Vec(nSets, new CacheEntry(tagBits, rowBytes)))

    val raddr = io.ifu.ar.bits.addr
    val ridx = raddr(indexBits + offsetBits - 1, offsetBits)
    val rtag = raddr(tagBits + indexBits + offsetBits - 1, indexBits + offsetBits)
    val roffset = raddr(offsetBits - 1, 0)


    val data_way = Wire(Vec(nWays, UInt((8*rowBytes).W)))
    for (i <- 0 until nWays){
        when(cache(i)(ridx).tag === rtag){
            data_way(i) := cache(i)(ridx).data.asUInt
        }.otherwise{
            data_way(i) := 0.U
        }
    }

    // offset 在这里暂时没什么用
    
    val hit_way = Wire(Vec(nWays, Bool()))
    for(i <- 0 until nWays){
        hit_way(i) :=  cache(i)(ridx).valid &&  cache(i)(ridx).tag === rtag
    }

    val hit = hit_way.reduce(_ || _)

    val rdata = Mux1H(hit_way, data_way)
    io.ifu.r.bits.data := rdata
    io.ifu.r.valid := hit || (state === s_bypass && io.imem.r.valid)

    // IF the address is not in the range of the SDRAM address, then it is a bypass
    val bypass = (io.ifu.ar.bits.addr(31,29) =/= "b101".U)


    val s_idle :: s_read :: s_replace :: s_refill :: s_bypass :: Nil = Enum(5)

    val state = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(Seq(
        s_idle -> Mux(io.ifu.r.valid, Mux(bypass, s_bypass, s_read), s_idle),
        s_read -> Mux(hit, s_idle, s_replace),
        s_replace -> Mux(io.imem.ar.valid, s_refill, s_replace),
        s_refill -> Mux(io.imem.r.valid, s_idle, s_refill),
        s_bypass -> Mux(io.imem.r.valid, s_idle, s_bypass)
    ))



    io.ifu.ar.ready := state === s_idle

    io.imem.ar.bits.addr := io.ifu.ar.bits.addr 
    // io.imem.ar.valid := state === s_fetch
    io.imem.r.ready := (state === s_refill) || (state === s_bypass && io.ifu.r.valid)

    // when(state === s_refill){
    //     val widx = LFSR16(wayBits)
    //     cache(widx)(ridx).valid := true.B
    //     cache(widx)(ridx).tag := rtag
    //     cache(widx)(ridx).data := io.imem.r.bits.data.asTypeOf(Vec(rowBytes, UInt(8.W)))
    // }

    when(state === s_bypass){
        io.ifu.r.bits.data := io.imem.r.bits.data
    }





    // // 访存状态机
    // val s_idle :: s_read ::s_wait_read :: s_wait_ready :: Nil = Enum(4)
    // val state = RegInit(s_idle)         
    //     state := MuxLookup(state, s_idle)(Seq(
    //     s_idle -> Mux(in_valid, s_read, s_idle),
    //     s_read -> Mux(io.mem.ar.ready, s_wait_read, s_read),
    //     s_wait_read -> Mux(io.mem.r.valid, s_wait_ready, s_wait_read),
    //     s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    // ))


}