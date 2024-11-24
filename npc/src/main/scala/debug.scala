package cpu 

import chisel3._
import chisel3.util._

/*
module InstInvalid(
    input isvalid
);
*/

class InstInvalid extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val isvalid = Input(Bool())
  })
}


/* 
module Ebreak(
    input isebreak
);
 */
class Ebreak extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val isebreak = Input(Bool())
  })
}