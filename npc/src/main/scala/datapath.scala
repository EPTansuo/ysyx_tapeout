package cpu

import chisel3._
import chisel3.util._


import control._
import top._


class datapath_io(xlen: Int) extends Bundle{
    val ctrlsig = Flipped(new control_io(xlen))
    val imemio = Flipped(new imem_io())
    val dmemio = Flipped(new dmem_io())
}


class datapath(xlen: Int) extends Module{
    val io = IO(new datapath_io(xlen))

    import defines._
    val regfile_ = Module(new regfile(XLEN))
    val alu_ = Module(new alu(XLEN))
    val immgen_ = Module(new immgen(XLEN))

    // val wb_sel = Wire(io.ctrlsig.wb_sel.cloneType)
    // val alu_op = Wire(io.ctrlsig.alu_op.cloneType)
    // val A_sel = Wire(io.ctrlsig.A_sel.cloneType)
    // val B_sel = Wire(io.ctrlsig.B_sel.cloneType)
    // val imm_sel = Wire(io.ctrlsig.imm_sel.cloneType)
    // val pc_sel = Wire(io.ctrlsig.pc_sel.cloneType)


    val pc = RegInit(PC_INIT.U(XLEN.W) - 4.U(XLEN.W))
    
    io.imemio.addr := pc
    
    import pc_sel._
    val npc = MuxLookup(io.ctrlsig.pc_sel, default = pc, Seq(
        PC_4.id.U   -> (pc + 4.U),
        PC_0.id.U   -> pc,
        PC_ALU.id.U -> alu_.io.out
    ))
    pc := npc
    
    var inst = io.imemio.data

    val rd_addr = inst(11, 7)
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    regfile_.io.raddr1 := rs1_addr
    regfile_.io.raddr2 := rs2_addr

    immgen_.io.inst := inst
    immgen_.io.sel := io.ctrlsig.imm_sel

    io.ctrlsig.inst := inst
    
    import A_sel._
    alu_.io.A := MuxLookup(io.ctrlsig.A_sel, default = 0.U(XLEN.W), Array(
        A_RS1.id.U -> regfile_.io.rdata1,
        A_PC.id.U  -> pc
        )
    )

    import B_sel._
    alu_.io.B := MuxLookup(io.ctrlsig.B_sel, default = 0.U(XLEN.W), Array(
        B_RS2.id.U -> regfile_.io.rdata2,
        B_IMM.id.U -> immgen_.io.out
        )
    )

    alu_.io.aluop := io.ctrlsig.alu_op

    import wb_sel._
    val wb_data = MuxLookup(io.ctrlsig.wb_sel, default = 0.U(XLEN.W), Array(
        WB_ALU.id.U -> alu_.io.out,
        WB_MEM.id.U -> io.dmemio.rdata
        )
    )

    regfile_.io.waddr := rd_addr
    regfile_.io.wdata := wb_data
    //regfile_.io.we := wb_sel =/= WB_X

    regfile_.io.we := io.ctrlsig.wb_sel =/= WB_XX.id.U;
    io.dmemio.addr := alu_.io.out
    
    io.dmemio.wdata := regfile_.io.rdata2
    io.dmemio.we := 0.U

}
