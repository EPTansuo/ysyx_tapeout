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



    // READ 
    val dmem_rdata_tmp = io.dmem.r.bits.data
    val dmem_rdata = Wire(UInt(xlen.W))//RegInit(0.U(xlen.W))
    val roffset = alu_out(1, 0) << 3.U 

    val dmem_rdata_reg = RegInit(VecInit(Seq(0.U(32.W), 0.U(32.W))))

    val r_twice_lh = ((ctrlsig.ld_sel === LD_LH || ctrlsig.ld_sel === LD_LHU)
                 && (alu_out(0) === 1.U) &&  (alu_out(1,0) === "b11".U))
    val r_twice_lw = ((ctrlsig.ld_sel === LD_LW) && (alu_out(1, 0) =/= 0.U))
    r_twice :=  r_twice_lh || r_twice_lw
    dontTouch(r_twice_lw)

    when(io.dmem.r.valid){
        when(state === s_wait_read){    
            dmem_rdata_reg(0) := dmem_rdata_tmp   // first read   addr = alu_out
        }.elsewhen(state === s_wait_read_2){
            dmem_rdata_reg(1) := dmem_rdata_tmp   // second read  addr = alu_out + 4
        }
    }
    
    dmem_rdata := Mux(r_twice,                   // t_twice = 1 && t_twice_lh = 1
                    Mux(r_twice_lh, Cat(dmem_rdata_reg(1)(23,0), dmem_rdata_reg(0)(31,24)), 
                    
                    MuxLookup(alu_out(1, 0), dmem_rdata_reg(0))(Seq(
                        0.U -> dmem_rdata_reg(0),
                        1.U -> Cat(dmem_rdata_reg(1)(7,  0), dmem_rdata_reg(0)(31, 8)),
                        2.U -> Cat(dmem_rdata_reg(1)(15, 0), dmem_rdata_reg(0)(31, 16)),
                        3.U -> Cat(dmem_rdata_reg(1)(23, 0), dmem_rdata_reg(0)(31, 24))
                    ))),   // t_twice = 1 && t_twice_lh = 0 

               (dmem_rdata_reg(0) >> roffset))   // t_twice = 0
    

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

        
    io.dmem.ar.valid := state === s_read || state === s_read_2
    io.dmem.ar.bits.addr := Mux(state === s_read_2 || state === s_wait_read_2, alu_out + 4.U, alu_out);
    io.dmem.ar.bits.prot := 0.U
    io.dmem.r.ready := true.B
    

    io.dmem.ar.bits.id := 0.U
    io.dmem.ar.bits.len := 0.U
    io.dmem.ar.bits.size := r_size
    io.dmem.ar.bits.burst := 0.U
    io.dmem.ar.bits.lock := 0.U
    io.dmem.ar.bits.cache := 0.U
    io.dmem.ar.bits.qos := 0.U



    // WRITE 
    val st_data_tmp = MuxLookup(ctrlsig.st_sel, 0.U(xlen.W))(Seq(
        ST_XX -> 0.U(xlen.W),
        ST_SB -> src2(7, 0),
        ST_SH -> src2(15, 0),
        ST_SW -> src2
        )
    )

    val woffset = alu_out(1, 0) << 3.U
    //val st_data = st_data_tmp << woffset
    val st_data_normal = MuxLookup(ctrlsig.st_sel, 0.U(xlen.W))(Seq(
        ST_XX -> 0.U(xlen.W),
        ST_SB -> Fill(3, st_data_tmp(7,0)),
        ST_SH -> Fill(2, st_data_tmp(15,0)),
        ST_SW -> st_data_tmp
        )
    )

    val w_size_normal = MuxLookup(ctrlsig.st_sel, 0.U(2.W))(Seq(
        ST_XX -> 0.U(2.W),
        ST_SB -> 0.U(2.W),  // 1 byte
        ST_SH -> 1.U(2.W),  // 2 bytes 
        ST_SW -> 2.U(2.W)   // 4 bytes
        )
    )
    val w_twice_sh = ((ctrlsig.st_sel === ST_SH) && alu_out(0) && (alu_out(1,0) === "b11".U)) 
    val w_twice_sw = ( ctrlsig.st_sel === ST_SW && alu_out(1, 0) =/= 0.U)
    w_twice := w_twice_sh || w_twice_sw

    // the First write
    val st_data_1 = Wire(UInt(xlen.W))
    val st_data_2 = Wire(UInt(xlen.W))

    st_data_1 := Mux(w_twice, 
                    Mux(w_twice_sh, Fill(3, st_data_normal(15, 8)), 
                    MuxLookup(alu_out(1, 0), st_data_normal)(Seq(
                        0.U -> st_data_normal,
                        1.U -> Cat(st_data_normal(23,0), Fill(8,"b0".U)),
                        2.U -> Cat(st_data_normal(15,0), Fill(16,"b0".U)),
                        3.U -> Cat(st_data_normal(7,0), Fill(24,"b0".U))
                    ))),
                st_data_normal
    )
    // the Second write
    st_data_2 := Mux(w_twice_sh, st_data_normal, // wmask should be 0b1000
                    MuxLookup(alu_out(1, 0), st_data_normal)(Seq(
                        0.U -> st_data_normal,
                        1.U -> Cat(Fill(24,"b0".U), st_data_normal(31, 24)),
                        2.U -> Cat(Fill(16,"b0".U), st_data_normal(31, 18)),
                        3.U -> Cat(Fill(8 ,"b0".U), st_data_normal(31, 8 ))
                    )))

    val st_data = Mux(state === s_write || state === s_wait_write, st_data_1, st_data_2)

    val wmask_normal = MuxLookup(ctrlsig.st_sel, 0.U(4.W))(Seq(
        ST_XX -> 0.U(4.W),
        ST_SB -> ("b0001".U << alu_out(1,0)),
        ST_SH -> (Mux(alu_out(1), "b1100".U, "b0011".U)),
        ST_SW -> "b1111".U
        )
    )

    val wmask_1 = Mux(w_twice,
                    Mux(w_twice_sh, "b0001".U,  // unaligned write "sh" 
                    MuxLookup(alu_out(1, 0), wmask_normal)(Seq( // unaligned write "sw"
                        0.U -> wmask_normal, 
                        1.U -> "b1110".U,
                        2.U -> "b1100".U,
                        3.U -> "b1000".U
                    ))),
                wmask_normal    // aligned write 
    )

    val wmask_2 = Mux(w_twice_sh, "b1000".U,  // unaligned write "sh"
                    MuxLookup(alu_out(1, 0), wmask_normal)(Seq( // unaligned write "sw"
                        0.U -> wmask_normal, 
                        1.U -> "b0001".U,
                        2.U -> "b0011".U,
                        3.U -> "b0111".U
                    )))
    
    val wmask = Mux(state === s_write || state === s_wait_write, wmask_1, wmask_2)

    io.dmem.aw.bits.id := 0.U
    io.dmem.aw.bits.len := 0.U
    io.dmem.aw.bits.burst := 0.U
    io.dmem.aw.bits.lock := 0.U
    io.dmem.aw.bits.cache := 0.U
    io.dmem.aw.bits.qos := 0.U


    io.dmem.aw.bits.size := 2.U//w_size_normal  // WARNING:  w_size_normal is used for both aligned and unaligned write
    io.dmem.w.bits.last := true.B
    io.dmem.aw.valid := state === s_write || state === s_write_2
    io.dmem.w.valid := state === s_write || state === s_write_2
    io.dmem.aw.bits.addr := Mux(state === s_write_2 || state === s_wait_write_2, alu_out + 4.U, alu_out);
    io.dmem.aw.bits.prot := 0.U
    io.dmem.w.bits.data := st_data
    io.dmem.w.bits.strb := wmask
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