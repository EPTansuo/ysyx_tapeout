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


    val btb = new Bundle {
        val pc = RegInit(VecInit(Seq.fill(2)(0.U(config.XLEN.W))))
        val npc = RegInit(VecInit(Seq.fill(2)(0.U(config.XLEN.W))))
    }

    val tag = RegInit(0.U(1.W))


    val match_result = Wire(UInt(32.W))
    match_result := io.pc + 4.U

    for (i <- 0 until 2) {
        when(btb.pc(i) === io.pc) {
        match_result := btb.npc(i)
        tag          := (i).U
        }
    }

    io.npc := match_result

    val replace_addr = ~tag

    when(io.update) {
        btb.pc(replace_addr)   := io.exu_pc
        btb.npc(replace_addr) := io.exu_npc
        tag                    := replace_addr
    }



}