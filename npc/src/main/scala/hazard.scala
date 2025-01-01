package cpu 

import chisel3._ 
import chisel3.util._  


// 控制相关  冲刷IFU、IDU 
class ControlHazard(config: NPCConfig) extends Module{
    val io = IO(new Bundle{
        val ifu_pc = Flipped(Decoupled(UInt(config.XLEN.W)))
        val idu_pc = Flipped(Decoupled(UInt(config.XLEN.W)))
        val exu_npc = Flipped(Decoupled(UInt(config.XLEN.W)))
        val flush = Output(Bool())
    })

    val ifu_flush = io.ifu_pc.valid && (io.exu_npc.bits =/= io.ifu_pc.bits + 8.U)
    val idu_flush = io.idu_pc.valid && (io.exu_npc.bits =/= io.idu_pc.bits + 4.U)

    io.flush := ifu_flush || idu_flush && io.exu_npc.valid

    io.ifu_pc.ready := true.B
    io.idu_pc.ready := true.B
    io.exu_npc.ready := true.B

}