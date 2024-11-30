package  cpu


import chisel3._
import chisel3.util._


object state_m {
    //val idle_m :: wait_ready_m = Enum(2) // Master 状态
    val write_m = 0.U(2.W)
    val wait_ready_m = 1.U(2.W)
    val idle_m = 2.U(2.W)
}

object state_s {
    //val idle_s :: wait_valid_s = Enum(2) // Slave 状态
    val read_s = 0.U(2.W)
    val wait_valid_s = 1.U(2.W)
}

class ComFSMIO extends Bundle{
    val valid = Input(Bool())
    val ready = Input(Bool())
    val state = Output(UInt(2.W))
}

class ComFSM_M_IO extends Bundle{
    val in_valid = Input(Bool())
    val in_ready = Input(Bool())
    val out_valid = Input(Bool())
    val out_ready = Input(Bool())
    val state = Output(UInt(2.W))
}


import state_m._

class ComFSM_M extends Module {
    val io = IO(new ComFSM_M_IO)
    val state_m = RegInit(idle_m)          // Master 状态寄存器
    state_m := MuxLookup(state_m, idle_m, Seq(
        (idle_m       -> Mux(io.in_valid & io.in_ready, wait_ready_m, idle_m)),
        (wait_ready_m -> Mux(io.out_valid, write_m, wait_ready_m)),
        (write_m       -> Mux(io.out_ready, idle_m, write_m))
))

    io.state := state_m
}

import state_s._

class ComFSM_S extends Module {
    val io = IO(new ComFSMIO)
    val state_s = RegInit(wait_valid_s)          // Slave 状态寄存器
    state_s := MuxLookup(state_s, read_s, Seq(
        (read_s       -> Mux(io.ready, wait_valid_s, read_s)),
        //(wait_valid_s -> Mux(io.ready & io.valid, idle_s, wait_valid_s))
        (wait_valid_s -> Mux(io.valid, read_s, wait_valid_s))
))

    io.state := state_s
}