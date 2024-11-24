package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._

import defines._ 


class CPUIO(xlen:Int) extends Bundle{
  val imem = Flipped(new IMemIO(xlen))
  val dmem = Flipped(new DMemIO())
}

class CPU(xlen:Int) extends Module{
  val io = IO(new CPUIO(xlen))


  io.imem.reset := reset 
  io.dmem.reset := reset
  io.dmem.clock := clock


  val alu = Module(new ALU(xlen))
  val idu = Module(new IDU(xlen))
  val ctrlsig = idu.io.out


  // pc 
  val pc = RegInit(PC_INIT.U(xlen.W) - 4.U(xlen.W))
  import pc_sel._
  val npc = MuxLookup(ctrlsig.pc_sel, default = pc, Seq(
      PC_4   -> (pc + 4.U),
      PC_0   -> pc,
      PC_ALU -> alu.io.out
  ))
  pc := npc
  
  // ifu
  val ifu = Module(new IFU(xlen))
  ifu.io.pc_in := pc
  io.imem.pc := ifu.io.mem_pc
  ifu.io.mem_inst := io.imem.data
  ifu.io.inst_out.ready := 1.U  // 写死
  var inst = ifu.io.inst_out.bits
  
  idu.io.inst := inst
  
  // regfile
  val regfile = Module(new Regfile(xlen))
  val rd_addr = inst(11, 7)
  val rs1_addr = inst(19, 15)
  val rs2_addr = inst(24, 20)
  regfile.io.raddr1 := rs1_addr
  regfile.io.raddr2 := rs2_addr
  regfile.io.waddr := rd_addr
  

  // immgen
  val immGen = Module(new ImmGen(xlen))
  immGen.io.inst := inst
  immGen.io.sel := ctrlsig.imm_sel



  
  import A_sel._
  alu.io.A := MuxLookup(ctrlsig.A_sel, default = 0.U(XLEN.W), Array(
      A_RS1 -> regfile.io.rdata1,
      A_PC  -> pc
      )
  )

  import B_sel._
  alu.io.B := MuxLookup(ctrlsig.B_sel, default = 0.U(XLEN.W), Array(
      B_RS2 -> regfile.io.rdata2,
      B_IMM -> immGen.io.out
      )
  )

  alu.io.aluop := ctrlsig.alu_op


  import ld_sel._
  val ld_data = MuxLookup(ctrlsig.ld_sel, default = 0.U(XLEN.W), Array(
      LD_XX -> 0.U(XLEN.W),
      LD_LB -> io.dmem.rdata(7, 0).asSInt.asUInt,
      LD_LH -> io.dmem.rdata(15, 0).asSInt.asUInt,
      LD_LW -> io.dmem.rdata,
      LD_LBU -> io.dmem.rdata(7, 0).asUInt,
      LD_LHU -> io.dmem.rdata(15, 0).asUInt
      )
  )

  import st_sel._
  val st_data = MuxLookup(ctrlsig.st_sel, default = 0.U(XLEN.W), Array(
      ST_XX -> 0.U(XLEN.W),
      ST_SB -> regfile.io.rdata2(7, 0),
      ST_SH -> regfile.io.rdata2(15, 0),
      ST_SW -> regfile.io.rdata2
      )
  )


  import wb_sel._
  val wb_data = MuxLookup(ctrlsig.wb_sel, default = 0.U(XLEN.W), Array(
      WB_ALU -> alu.io.out,
      WB_MEM -> ld_data,
      WB_PC  -> pc
      )
  )

  

  regfile.io.wdata := wb_data
  regfile.io.we := ctrlsig.wb_sel =/= WB_XX;

  io.dmem.raddr := alu.io.out
  io.dmem.we := ctrlsig.st_sel =/= ST_XX
  io.dmem.waddr := alu.io.out
  io.dmem.wdata := st_data
  io.dmem.wmask := ctrlsig.mask_sel
}
