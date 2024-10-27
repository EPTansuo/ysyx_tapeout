package cpu

import chisel3._
import chisel3.util._
import chisel3.stage._

import defines._ 
import top._ 


class cpu_io extends Bundle{
  val imem = Flipped(new imem_io())
  val dmem = Flipped(new dmem_io())

  //JUST FOR EXPOSING THE SIGNAL
  val control_io = (new control_io(XLEN))
}

class cpu extends Module{
  val io = IO(new cpu_io())

  val datapath_ = Module(new datapath(XLEN))
  val control_ = Module(new control(XLEN))

  datapath_.io.ctrlsig <> control_.io
  
  //JUST FOR EXPOSING THE SIGNAL
  io.control_io <> control_.io

  datapath_.io.imemio <> io.imem

  datapath_.io.dmemio <> io.dmem
  

}
