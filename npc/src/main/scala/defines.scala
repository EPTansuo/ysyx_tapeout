package cpu

import chisel3._

  object defines {
    val XLEN = 32
    val PC_INIT = "h80000000".U(XLEN.W)
  }

