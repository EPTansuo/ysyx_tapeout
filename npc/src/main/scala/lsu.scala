package cpu

import chisel3._
import chisel3.util._ 

import st_sel._
import ld_sel._

import AXI4._ 
import defines._
import freechips.rocketchip.amba.axi4._


class LSU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_EXU_LSU(xlen)))
        val out = (Decoupled(new SigIO_LSU_WBU(xlen)))
        //val dmem = Flipped(new DMemIO())
        //val dmem = new AXILiteMasterIF(addrWidthBits = 32, dataWidthBits = xlen)
        val dmem = new AXI4Bundle(AXI4BundleParameters(xlen, 32, AXI_IDBITS))
    })


    val in_reg = Reg(Output(chiselTypeOf(io.in)))
    val pc = in_reg.bits.pc
    val inst = in_reg.bits.inst
    val ctrlsig = in_reg.bits.lsu
    val src1 = in_reg.bits.src1
    val src2 = in_reg.bits.src2 
    val alu_out = in_reg.bits.alu_out
    val rd_addr = in_reg.bits.rd_addr
    val wbu_data = in_reg.bits.wbu
    val npc = in_reg.bits.npc
    val store_en = ctrlsig.st_sel =/= ST_XX
    val load_en = ctrlsig.ld_sel =/= LD_XX

val s_idle :: s_exe :: s_read :: s_wait_read :: s_read_2 :: s_wait_read_2 :: s_write :: s_wait_write :: s_write_2 :: s_wait_write_2 :: s_wait_ready :: Nil = Enum(11)

    val r_twice = Wire(Bool());    // Must Read/Write twice because of unaligned access
    val w_twice = Wire(Bool());

    val state = RegInit(s_idle)         
    state := MuxLookup(state, s_idle)(Seq(
        s_idle -> Mux(io.in.valid, s_exe, s_idle),
        s_exe  -> Mux(load_en, s_read, Mux(store_en, s_write, s_wait_ready)),  //需要等待信号生成完毕，来判断是否需要读写数据
        s_read         -> Mux(io.dmem.ar.ready, s_wait_read, s_read),
        s_read_2       -> Mux(io.dmem.ar.ready, s_wait_read_2, s_read_2),
        s_wait_read    -> Mux(io.dmem.r.valid, Mux(r_twice, s_read_2, s_wait_ready), s_wait_read),
        s_wait_read_2  -> Mux(io.dmem.r.valid, s_wait_ready, s_wait_read_2),
        s_write        -> Mux(io.dmem.aw.ready && io.dmem.w.ready, s_wait_write, s_write),
        s_write_2      -> Mux(io.dmem.aw.ready && io.dmem.w.ready, s_wait_write_2, s_write_2),
        s_wait_write_2 -> Mux(io.dmem.b.valid, s_wait_ready, s_wait_write_2),
        s_wait_write   -> Mux(io.dmem.b.valid, Mux(w_twice, s_write_2,s_wait_ready), s_wait_write),
        s_wait_ready   -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))


    io.out.valid := state === s_wait_ready
    io.in.ready := state === s_idle

    when( io.in.valid && io.in.ready){
        in_reg := io.in
    }



    val dmem_rdata_tmp = io.dmem.r.bits.data
    val dmem_rdata = RegInit(0.U(xlen.W))
    val roffset = alu_out(1, 0) << 3.U 


    when(io.dmem.r.valid){
        dmem_rdata := dmem_rdata_tmp >> roffset
    }

    val ld_data = MuxLookup(ctrlsig.ld_sel, 0.U(xlen.W))(Seq(
        LD_XX -> 0.U(xlen.W),
        LD_LB -> Cat(Fill(xlen-8, dmem_rdata(7)), dmem_rdata(7, 0)),
        LD_LH -> Cat(Fill(xlen-16, dmem_rdata(15)), dmem_rdata(15, 0)),
        LD_LW -> dmem_rdata,
        LD_LBU -> dmem_rdata(7, 0).asUInt,
        LD_LHU -> dmem_rdata(15, 0).asUInt
        )
    )
    val r_size = MuxLookup(ctrlsig.ld_sel, 0.U(2.W))(Seq(
        LD_XX -> 0.U(2.W),
        LD_LB -> 0.U(2.W),  // 1 byte
        LD_LH -> 1.U(2.W),  // 2 bytes 
        LD_LW -> 2.U(2.W),  // 4 bytes
        LD_LBU -> 0.U(2.W),
        LD_LHU -> 1.U(2.W)
        )
    )

    r_twice := ((ctrlsig.ld_sel === LD_LH || ctrlsig.ld_sel === LD_LHU)
                 && (alu_out(0) === 1.U) &&  (alu_out(1,0) === "b11".U)) || 
                (ctrlsig.ld_sel === LD_LW && alu_out(1, 0) =/= 0.U)



    io.dmem.ar.valid := state === s_read || state === s_read_2
    io.dmem.ar.bits.addr := alu_out
    io.dmem.ar.bits.prot := 0.U
    io.dmem.r.ready := true.B
    

    io.dmem.ar.bits.id := 0.U
    io.dmem.ar.bits.len := 0.U
    io.dmem.ar.bits.size := r_size
    io.dmem.ar.bits.burst := 0.U
    io.dmem.ar.bits.lock := 0.U
    io.dmem.ar.bits.cache := 0.U
    io.dmem.ar.bits.qos := 0.U



    val st_data_tmp = MuxLookup(ctrlsig.st_sel, 0.U(xlen.W))(Seq(
        ST_XX -> 0.U(xlen.W),
        ST_SB -> src2(7, 0),
        ST_SH -> src2(15, 0),
        ST_SW -> src2
        )
    )

    val woffset = alu_out(1, 0) << 3.U
    //val st_data = st_data_tmp << woffset
    val st_data = MuxLookup(ctrlsig.st_sel, 0.U(xlen.W))(Seq(
        ST_XX -> 0.U(xlen.W),
        ST_SB -> Fill(4, st_data_tmp(7,0)),
        ST_SH -> Fill(2, st_data_tmp(15,0)),
        ST_SW -> st_data_tmp
        )
    )

    val w_size = MuxLookup(ctrlsig.st_sel, 0.U(2.W))(Seq(
        ST_XX -> 0.U(2.W),
        ST_SB -> 0.U(2.W),  // 1 byte
        ST_SH -> 1.U(2.W),  // 2 bytes 
        ST_SW -> 2.U(2.W)   // 4 bytes
        )
    )

    w_twice := ((ctrlsig.st_sel === ST_SH) && alu_out(0) && (alu_out(1,0) === "b11".U)) ||
               ( ctrlsig.st_sel === ST_SW && alu_out(1, 0) =/= 0.U)
 

    io.dmem.aw.bits.id := 0.U
    io.dmem.aw.bits.len := 0.U
    io.dmem.aw.bits.burst := 0.U
    io.dmem.aw.bits.lock := 0.U
    io.dmem.aw.bits.cache := 0.U
    io.dmem.aw.bits.qos := 0.U


    io.dmem.aw.bits.size := w_size
    io.dmem.w.bits.last := true.B
    io.dmem.aw.valid := state === s_write
    io.dmem.w.valid := state === s_write
    io.dmem.aw.bits.addr := alu_out
    io.dmem.aw.bits.prot := 0.U
    io.dmem.w.bits.data := st_data
    io.dmem.w.bits.strb := MuxLookup(ctrlsig.st_sel, 0.U(4.W))(Seq(
        ST_XX -> 0.U(4.W),
        ST_SB -> ("b0001".U << alu_out(1,0)),
        ST_SH -> (Mux(alu_out(1), "b1100".U, "b0011".U)),
        ST_SW -> "b1111".U
        )
    )
    io.dmem.b.ready := (state === s_wait_write || state === s_wait_write_2) && io.dmem.b.valid

    io.out.bits.inst := inst
    io.out.bits.pc := pc
    io.out.bits.alu_out := alu_out
    io.out.bits.rd_addr := rd_addr
    io.out.bits.ld_data := ld_data
    io.out.bits.src1 := src1
    io.out.bits.wbu <> wbu_data
    io.out.bits.npc := npc 
    io.out.bits.csr_out := io.in.bits.csr_out

}