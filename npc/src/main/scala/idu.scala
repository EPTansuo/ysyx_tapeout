package cpu

import chisel3._
import chisel3.util._




//import defines._

class IDU(config: NPCConfig) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new SigIO_IFU_IDU(config.XLEN)))
        //val out = Output(new ControlOut(xlen))
        val out = (Decoupled(new SigIO_IDU_EXU(config.XLEN)))
        val flush = Input(Bool())
        val pc = Decoupled(UInt(config.XLEN.W))
        val inst_type = Output(UInt(4.W))
        val stall = Input(Bool())
    })

    
    val control = Module(new Control(config))
    val inst = io.in.bits.inst 
    val pc = io.in.bits.pc
    val stall = io.stall
    // val inst = RegInit(0.U(32.W))
    // val pc = RegInit(0.U(32.W))

    val s_idle :: s_wait_ready :: Nil = Enum(2)

    val state = RegInit(s_idle)         
    state := MuxLookup(state, s_idle)(Seq(
        s_idle -> Mux(io.in.valid, s_wait_ready, s_idle),
        s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
    ))


    io.out.valid := state === s_wait_ready && ~stall
    io.in.ready := state === s_idle && ~stall 

    when(io.flush){
        state := s_idle
    }

    io.pc.bits := pc
    io.pc.valid := state =/= s_idle  && ~stall
    // when( io.in.valid && io.in.ready){
    //     inst := io.in.bits.inst
    //     pc := io.in.bits.pc
    // }



    control.io.in.inst := inst 
    control.io.in.pc := pc 

    io.out.bits.inst := inst
    io.out.bits.pc := pc
    io.out.bits.exu.A_sel := control.io.out.A_sel
    io.out.bits.exu.B_sel := control.io.out.B_sel
    io.out.bits.exu.alu_op := control.io.out.alu_op
    io.out.bits.exu.imm_sel := control.io.out.imm_sel
    io.out.bits.lsu.ld_sel := control.io.out.ld_sel
    io.out.bits.lsu.st_sel := control.io.out.st_sel
    io.out.bits.wbu.wb_sel := control.io.out.wb_sel
    io.out.bits.wbu.csr_cmd := control.io.out.csr_cmd
    io.out.bits.exu.br_sel := control.io.out.br_sel
    io.out.bits.exu.pc_sel := control.io.out.pc_sel

    io.inst_type := control.io.out.inst_type

      //Ebreak
    val ebreak_ = Module(new Ebreak)
    val isebreak = inst === insts.ebreak
    ebreak_.io.isebreak := isebreak
    //invaild instruction
    val instInvalid = Module(new InstInvalid)
    instInvalid.io.isvalid := Mux(io.out.valid, control.io.out.inst_valid === valid.INST_VALID || isebreak, true.B)



    if(config.PERF_CNT){
        val inst_compute_cnt = RegInit(0.U(32.W))
        val inst_branch_cnt = RegInit(0.U(32.W))
        val inst_ldst_cnt = RegInit(0.U(32.W))
        val inst_csr_cnt = RegInit(0.U(32.W))
        val inst_jump_cnt = RegInit(0.U(32.W))
        val cycle_compute_cnt = RegInit(0.U(64.W))
        val cycle_branch_cnt = RegInit(0.U(64.W))
        val cycle_ldst_cnt = RegInit(0.U(64.W))
        val cycle_csr_cnt = RegInit(0.U(64.W))
        val cycle_jump_cnt = RegInit(0.U(64.W))
        val sig = control.io.out
        import pc_sel._
        import br_sel._
        import ld_sel._
        import st_sel._
        import csr_cmd._

        when(io.out.valid && io.out.ready){
            when(sig.csr_cmd =/= CSR_XX){
                inst_csr_cnt := inst_csr_cnt + 1.U
            }.elsewhen(sig.br_sel =/= BR_XX){
                inst_branch_cnt := inst_branch_cnt + 1.U
            }.elsewhen(sig.ld_sel =/= LD_XX || sig.st_sel =/= ST_XX){
                inst_ldst_cnt := inst_ldst_cnt + 1.U
            }.elsewhen(sig.pc_sel === PC_ALU){
                inst_jump_cnt := inst_jump_cnt + 1.U
            }.otherwise{
                inst_compute_cnt := inst_compute_cnt + 1.U
            }
        }
        when(sig.csr_cmd =/= CSR_XX){
            cycle_csr_cnt := cycle_csr_cnt + 1.U
        }.elsewhen(sig.br_sel =/= BR_XX){
            cycle_branch_cnt := cycle_branch_cnt + 1.U
        }.elsewhen(sig.ld_sel =/= LD_XX || sig.st_sel =/= ST_XX){
            cycle_ldst_cnt := cycle_ldst_cnt + 1.U
        }.elsewhen(sig.pc_sel === PC_ALU){
            cycle_jump_cnt := cycle_jump_cnt + 1.U
        }.otherwise{
            cycle_compute_cnt := cycle_compute_cnt + 1.U
        }
        dontTouch(inst_compute_cnt)
        dontTouch(inst_branch_cnt)
        dontTouch(inst_ldst_cnt)
        dontTouch(inst_csr_cnt)
        dontTouch(inst_jump_cnt)
        dontTouch(cycle_compute_cnt)
        dontTouch(cycle_branch_cnt)
        dontTouch(cycle_ldst_cnt)
        dontTouch(cycle_csr_cnt)
        dontTouch(cycle_jump_cnt)
    }

}