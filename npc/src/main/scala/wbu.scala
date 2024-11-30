package  cpu


import chisel3._
import chisel3.util._

import wb_sel._

import state_m._
import state_s._

class WBU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_LSU_WBU(xlen)))
 //       val out = Decoupled(new SIGIO_WBU_IFU(xlen))
        val reg_write = Flipped(new RegfileWriteIO(xlen))
        val csr_out = Input(UInt(xlen.W))
        val csr_in = Output(UInt(xlen.W))
    })
    val ctrlsig = io.in.bits.wbu
    val alu_out = io.in.bits.alu_out
    val ld_data = io.in.bits.ld_data
    val src1 = io.in.bits.src1
    val pc = io.in.bits.pc


    val fsm_s = Module(new ComFSM_S)
    val state_s = fsm_s.io.state
    fsm_s.io.valid := io.in.valid
    fsm_s.io.ready := io.in.ready

    when(state_s === idle_s){
        io.in.ready := 1.U
    }.otherwise{
        io.in.ready := 0.U 
    }


    io.reg_write.addr := io.in.bits.rd_addr
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
