package cpu 


import chisel3._
import chisel3.util._




class Sig_EXU extends Bundle{
    val A_sel = Input(UInt(1.W))
    val B_sel = Input(UInt(1.W))
    val alu_op = Input(UInt(4.W))
    val imm_sel = Input(UInt(3.W))
}

class Sig_LSU extends  Bundle{
    val ld_sel = Input(UInt(3.W))
    val st_sel = Input(UInt(2.W))
}

class Sig_WBU extends Bundle{
    val wb_sel = Input(UInt(3.W))
}

class Sig_CSR extends Bundle{
    val csr_cmd = Input(UInt(3.W))
}


class SigIO_IDU_EXU(xlen: Int) extends Bundle{
    val exu = new Sig_EXU
    val lsu = new Sig_LSU
    val wbu = new Sig_WBU
    val inst = Input(UInt(32.W))
    val pc = Input(UInt(xlen.W))
}

class SigIO_EXU_LSU(xlen: Int) extends Bundle{
    val lsu = new Sig_LSU
    val wbu = new Sig_WBU
    val inst = Input(UInt(32.W))
    val pc = Input(UInt(xlen.W))
}

class SigIO_LSU_WBU(xlen: Int) extends Bundle{
    val wbu = new Sig_WBU
    val inst = Input(UInt(32.W))
    val pc = Input(UInt(xlen.W))
}