package cpu

import chisel3._
import chisel3.util._


import Control._
import defines._

class IDU(xlen: Int) extends Module {
    val io = IO(new Bundle {
        val inst = Input(UInt(xlen.W))
        val out = Output(new ControlOut(xlen))
    })


    val control = Module(new Control(xlen))

    control.io.in.inst := io.inst
    control.io.out <> io.out

}