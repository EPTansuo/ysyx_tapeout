package AXI4 

import chisel3._
import chisel3.util._


class AXI4LiteArbiter(nMasters: Int, addrWidthBits: Int, dataWidthBits: Int) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(nMasters, new AXILiteMasterIF(addrWidthBits, dataWidthBits))
    val slave = new AXILiteSlaveIF(addrWidthBits, dataWidthBits)
  })

  // 读操作仲裁
  val readArbitration = PriorityEncoder(io.masters.map(_.readAddr.valid))

  // 读操作传输
  io.slave.readAddr.valid := io.masters(readArbitration).readAddr.valid
  io.slave.readAddr.bits := io.masters(readArbitration).readAddr.bits
  io.masters(readArbitration).readData.valid := io.slave.readData.valid
  io.masters(readArbitration).readData.bits := io.slave.readData.bits

  // 写操作仲裁
  val writeArbitration = PriorityEncoder(io.masters.map(_.writeAddr.valid))

  // 写操作传输
  io.slave.writeAddr.valid := io.masters(writeArbitration).writeAddr.valid
  io.slave.writeAddr.bits := io.masters(writeArbitration).writeAddr.bits
  io.slave.writeData.valid := io.masters(writeArbitration).writeData.valid
  io.slave.writeData.bits := io.masters(writeArbitration).writeData.bits
  io.masters(writeArbitration).writeResp.valid := io.slave.writeResp.valid
  io.masters(writeArbitration).writeResp.bits := io.slave.writeResp.bits

  // 每个主设备的 ready 信号
  (0 until nMasters).foreach { i =>
    // 读操作
    io.masters(i).readAddr.ready := io.slave.readAddr.ready && readArbitration === i.U
    io.masters(i).readData.ready := io.slave.readData.ready && readArbitration === i.U

    // 写操作
    io.masters(i).writeAddr.ready := io.slave.writeAddr.ready && writeArbitration === i.U
    io.masters(i).writeData.ready := io.slave.writeData.ready && writeArbitration === i.U
    io.masters(i).writeResp.ready := io.slave.writeResp.ready && writeArbitration === i.U
  }
}