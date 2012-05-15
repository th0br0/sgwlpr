package name.mkdir.gwlpr.game

import name.mkdir.gwlpr.Packet
import name.mkdir.gwlpr.PacketAnnotations._

// C2S
case class ClientVerificationPacket(
  data1: Int,
  data2: Long,
  data3: Long,
  @ArrayInfo(constSize = true, size = 4) securityKey1: Array[Byte],
  data4: Long,
  @ArrayInfo(constSize = true, size = 4) securityKey2: Array[Byte],
  @ArrayInfo(constSize = true, size = 16) accountHash: Array[Byte],
  @ArrayInfo(constSize = true, size = 16) characterHash: Array[Byte],
  data5: Long,
  data6: Long) extends Packet(1280)


