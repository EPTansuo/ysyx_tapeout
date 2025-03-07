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
    val params = config.bpuparameters
    val entrySize = params.nEntries
    val tagWidth = log2Ceil(entrySize)
    val pcWidth = params.pcWidth
    val npcWidth = params.npcWidth
    val cntWidth = params.cntWidth

    val btb = new Bundle {
        val pc = RegInit(VecInit(Seq.fill(entrySize)(0.U(pcWidth.W))))
        val npc = RegInit(VecInit(Seq.fill(entrySize)(0.U(npcWidth.W))))
        val counter = RegInit(VecInit(Seq.fill(entrySize)(0.U(cntWidth.W)))) // 2-bit saturating counter
    }
    

    val tag = RegInit(0.U(tagWidth.W))


    val npc = Wire(UInt(config.XLEN.W))

    val branch_taken = io.exu_npc =/= io.exu_pc + 4.U

    npc := io.pc + 4.U
    for (i <- 0 until entrySize) {
        when(btb.pc(i) === io.pc(pcWidth-1,0)) {
            when(btb.counter(i)(cntWidth-1)){
                npc := Cat(io.pc(config.XLEN-1, npcWidth), btb.npc(i))
            }.otherwise{
                npc := io.pc + 4.U
            }
        tag := (i).U 
        }
    }

    val replace_addr = tag + 1.U

    // when(io.update) {
    //     btb.pc(replace_addr) := io.exu_pc
    //     btb.npc(replace_addr) := io.exu_npc
    //     tag := replace_addr
    // }


    val update_idx = WireDefault(entrySize.U) 
    for (i <- 0 until entrySize) {
        when(btb.pc(i) === io.exu_pc(pcWidth-1, 0)) {
            update_idx := i.U 
        }
    }

    when(io.update) {
        when(update_idx < entrySize.U) {
            when(branch_taken) {
                btb.counter(update_idx) := Mux(btb.counter(update_idx) === 3.U, 3.U, btb.counter(update_idx) + 1.U)
            }.otherwise {
                btb.counter(update_idx) := Mux(btb.counter(update_idx) === 0.U, 0.U, btb.counter(update_idx) - 1.U)
            }
        }.otherwise {
            btb.pc(replace_addr)    := io.exu_pc(pcWidth-1, 0)
            btb.npc(replace_addr)   := io.exu_npc(npcWidth-1, 0)
            btb.counter(replace_addr) := Mux(branch_taken, 3.U, 1.U) // 强采取或弱不采取
            tag := replace_addr
        }
    }


    io.npc := npc
}