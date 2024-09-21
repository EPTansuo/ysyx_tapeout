package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._
import common.Constants._

class cpu extends Module{
  val io = IO(new Bundle{
    val imem = Flipped(new IMemPortIO())
    val exit = Output(Bool())
  })

  val regfile = Mem(32, UInt(XLEN.W))
  val pc_reg = RegInit(PC_INIT)

  pc_reg := pc_reg + 4.U(XLEN.W)
  io.imem.addr := pc_reg
  val inst = io.imem.inst

  io.exit := (inst === 0x00000000.U(WORD_LEN.W))

}
