package cpu

import chisel3._
import chisel3.util._

class BPU(config:NPCConfig) extends Module{
    val io = IO(new Bundle{
        val pc = Input(UInt(config.XLEN.W))
        val update = Input(Bool())
        val exu_npc = Input(UInt(config.XLEN.W))
        val exu_pc = Input(UInt(config.XLEN.W))
        val npc = Output(UInt(config.XLEN.W))

    })

    val entrySize = 2
    val tagWidth = log2Ceil(entrySize)

    val btb = new Bundle {
        val pc = RegInit(VecInit(Seq.fill(entrySize)(0.U(config.XLEN.W))))
        val npc = RegInit(VecInit(Seq.fill(entrySize)(0.U(config.XLEN.W))))
    }

    val tag = RegInit(0.U(tagWidth.W))


    val npc = Wire(UInt(32.W))

    npc := io.pc + 4.U
    for (i <- 0 until entrySize) {
        when(btb.pc(i) === io.pc) {
            npc := btb.npc(i)
            tag := (i).U
        }
    }

    val replace_addr = tag + 1.U

    when(io.update) {
        btb.pc(replace_addr) := io.exu_pc
        btb.npc(replace_addr) := io.exu_npc
        tag := replace_addr
    }

    io.npc := npc
}