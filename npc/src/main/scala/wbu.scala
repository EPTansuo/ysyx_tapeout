package  cpu


import chisel3._
import chisel3.util._

import wb_sel._




class WBU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_LSU_WBU(xlen)))
        val out = Decoupled(new SigIO_WBU_IFU(xlen))
        val reg_write = Flipped(new RegfileWriteIO(xlen))
    })

    val in_reg = Reg(Output(chiselTypeOf(io.in)))
    val pc = in_reg.bits.pc
    val ctrlsig = in_reg.bits.wbu
    val src1 = in_reg.bits.src1
    val alu_out = in_reg.bits.alu_out
    val ld_data = in_reg.bits.ld_data
    val rd_addr = in_reg.bits.rd_addr
    val npc = in_reg.bits.npc
    val csr_out = in_reg.bits.csr_out

    val s_idle :: s_wait_ready :: Nil = Enum(2)

    val state = RegInit(s_idle)         
    state := MuxLookup(state, s_idle, Seq(
        s_idle -> Mux(io.in.valid, s_wait_ready, s_idle),
        s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))


    io.out.valid := state === s_wait_ready
    io.in.ready := state === s_idle

    when( io.in.valid && io.in.ready){
        in_reg := io.in
    }
   

    val valid_old = RegNext(io.out.valid)
    val wbu_valid = !valid_old && io.out.valid 
    dontTouch(wbu_valid) // Used for simulation
    dontTouch(pc)
    dontTouch(npc)

    io.reg_write.addr := rd_addr
    io.reg_write.en := ctrlsig.wb_sel =/= WB_XX;
        
    val wb_data = MuxLookup(ctrlsig.wb_sel, default = 0.U(xlen.W), Array(
        WB_ALU -> alu_out,
        WB_MEM -> ld_data,
        WB_PC4  -> (pc + 4.U),
        WB_CSR -> csr_out,
        )
    )
    io.reg_write.data := wb_data


    io.out.bits.npc := npc

}
