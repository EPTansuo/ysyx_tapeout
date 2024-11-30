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

class ComFSM_MIO extends Bundle{
    val valid = Input(Bool())
    val ready = Input(Bool())
    val state = Output(UInt(1.W))
}

import state_m._

class ComFSM_M extends Module {
    val io = IO(new ComFSMIO)
    val state_m = RegInit(write_m)          // Master 状态寄存器
    state_m := MuxLookup(state_m, write_m, Seq(
        (write_m       -> Mux(io.valid, wait_ready_m, write_m)),
        (wait_ready_m -> Mux(io.ready, write_m, wait_ready_m))
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