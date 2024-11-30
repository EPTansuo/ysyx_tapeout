package  cpu


import chisel3._
import chisel3.util._

import wb_sel._

import state_s._
import state_m._

class WBU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_LSU_WBU(xlen)))
        val out = Decoupled(new SigIO_WBU_IFU(xlen))
        val reg_write = Flipped(new RegfileWriteIO(xlen))
        val csr_out = Input(UInt(xlen.W))
        val csr_in = Output(UInt(xlen.W))
    })

    val in_reg = Reg(Output(chiselTypeOf(io.in)))
    val pc = in_reg.bits.pc
    val ctrlsig = in_reg.bits.wbu
    val src1 = in_reg.bits.src1
    val alu_out = in_reg.bits.alu_out
    val ld_data = in_reg.bits.ld_data
    val rd_addr = in_reg.bits.rd_addr

    val fsm_s = Module(new ComFSM_S)
    val state_s = fsm_s.io.state
    fsm_s.io.valid := io.in.valid
    fsm_s.io.ready := io.in.ready

    when(state_s === read_s){
        in_reg := io.in
    }
    io.in.ready := state_s === read_s

   
    val fsm_m = Module(new ComFSM_M)
    val state_m = fsm_m.io.state

    fsm_m.io.out_valid := io.out.valid
    fsm_m.io.out_ready := io.out.ready
    fsm_m.io.in_valid := io.in.valid
    fsm_m.io.in_ready := io.in.ready

    io.out.valid := state_m === wait_ready_m || state_m === write_m
    

    io.reg_write.addr := rd_addr
    io.reg_write.en := ctrlsig.wb_sel =/= WB_XX;
        
    val wb_data = MuxLookup(ctrlsig.wb_sel, default = 0.U(xlen.W), Array(
        WB_ALU -> alu_out,
        WB_MEM -> ld_data,
        WB_PC4  -> (pc + 4.U),
        WB_CSR -> io.csr_out,
        )
    )
    io.reg_write.data := wb_data


    io.csr_in := src1  //目前还未用到立即数  WARNING

    
}
