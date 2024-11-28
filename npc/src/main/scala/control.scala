package cpu

import chisel3._
import chisel3.util._
import insts._
import defines._




class ControlOut(xlen: Int) extends Bundle{
  val pc_sel = Output(UInt(2.W))
  val A_sel = Output(UInt(1.W))
  val B_sel = Output(UInt(1.W))
  val alu_op = Output(UInt(4.W))
  val imm_sel = Output(UInt(3.W))
  val wb_sel = Output(UInt(3.W))
  val ld_sel = Output(UInt(3.W))
  val st_sel = Output(UInt(2.W))
  val mask_sel = Output(UInt(8.W))
  val br_sel =  Output(UInt(3.W))
  val csr_cmd = Output(UInt(3.W))
}

class ControlIn(xlen: Int) extends Bundle{
    val inst = Input(UInt(xlen.W))
    val pc = Input(UInt(xlen.W))
}

class Control(xlen: Int) extends Module{
  val io = IO(new Bundle{
    val out = Output(new ControlOut(xlen))
    val in = Input(new ControlIn(xlen))
  })
  val ctrlsig = ListLookup(io.in.inst, SigMap.default, SigMap.map)
  io.out.pc_sel := ctrlsig(0)
  io.out.B_sel := ctrlsig(2)
  io.out.A_sel := ctrlsig(1)
  io.out.alu_op := ctrlsig(3)
  io.out.imm_sel := ctrlsig(4)
  io.out.wb_sel := ctrlsig(5)
  io.out.ld_sel := ctrlsig(6)
  io.out.st_sel := ctrlsig(7)
  io.out.mask_sel := ctrlsig(8)
  io.out.br_sel := ctrlsig(9)
  io.out.csr_cmd := ctrlsig(10)
  //Ebreak
  val ebreak_ = Module(new Ebreak)
  val isebreak = io.in.inst === insts.ebreak
  ebreak_.io.isebreak := isebreak

  //invaild instruction
  val instInvalid = Module(new InstInvalid)
  instInvalid.io.isvalid := (ctrlsig(11) === valid.INST_VALID) ||
                          isebreak || io.in.pc < PC_INIT
}


