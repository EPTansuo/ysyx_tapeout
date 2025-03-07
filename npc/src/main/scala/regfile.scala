package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._

class RegfileReadIO(xlen:Int) extends Bundle{
    var addr = Input(UInt(5.W))
    var data = Output(UInt(xlen.W))
}

class RegfileWriteIO(xlen:Int) extends Bundle{
    var en = Input(Bool())
    var addr = Input(UInt(5.W))
    var data = Input(UInt(xlen.W))
}

class RegfileIO(xlen: Int) extends Bundle{
    val read1 = new RegfileReadIO(xlen)
    val read2 = new RegfileReadIO(xlen)
    val write = new RegfileWriteIO(xlen)
}


class Regfile(config: NPCConfig) extends Module{
    val io = IO(new RegfileIO(config.XLEN))
/*
    var regs = Mem(config.REG_NUM, UInt(config.XLEN.W))

    // read data with internal forwarding 
    io.read1.data := Mux(io.write.addr === io.read1.addr && io.write.addr =/= 0.U && io.write.en, 
                            io.write.data, regs(io.read1.addr))
    io.read2.data := Mux(io.write.addr === io.read2.addr && io.write.addr =/= 0.U && io.write.en, 
                            io.write.data, regs(io.read2.addr))
    

    // io.read1.data := regs(io.read1.addr)
    // io.read2.data := regs(io.read2.addr)

    when(io.write.en) {
        regs(io.write.addr) := Mux(io.write.addr === 0.U, 0.U, io.write.data)
    }
*/

    var regs = Mem(config.REG_NUM - 1, UInt(config.XLEN.W))

    // read data with internal forwarding 

    io.read1.data := Mux(io.read1.addr.orR, Mux(io.write.addr === io.read1.addr, 
                            io.write.data, regs(io.read1.addr - 1.U )), 0.U)
    io.read2.data := Mux(io.read2.addr.orR, Mux(io.write.addr === io.read2.addr, 
                            io.write.data, regs(io.read2.addr - 1.U )), 0.U)
    

    when(io.write.en && io.write.addr.orR) {
        regs(io.write.addr - 1.U) := io.write.data
    }
}
