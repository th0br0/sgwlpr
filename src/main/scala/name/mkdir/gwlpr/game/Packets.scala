package name.mkdir.gwlpr.game

import name.mkdir.gwlpr.Config

import name.mkdir.gwlpr.PacketAnnotations._
import name.mkdir.gwlpr.Packet

// ------------------------------------------------
// ------------------- C 2 S ----------------------
// ------------------------------------------------

case class RequestSpawnPointPacket extends Packet(129)

// --------------- UNKNOWN PACKETS ----------------
case class Packet19(data1: Long, data2: Long) extends Packet(19)

// ------------------------------------------------
// ------------------- S 2 C ----------------------
// ------------------------------------------------

case class InstanceLoadHeaderPacket(data1: Byte, data2: Byte, @ArrayInfo(constSize = true, size = 2) data3: Array[Byte] = new Array[Byte](2)) extends Packet(370) 

object InstanceLoadHeaderPacket {
  def apply(isOutpost: Boolean) : InstanceLoadHeaderPacket = {
    val byte = {
      if(isOutpost) 0x3F
      else 0x1F
    }.toByte
    InstanceLoadHeaderPacket(byte, byte)
  }

}

// ---------UNKNOWN, CHAR CREATION RELEVANT--------
case class Packet379 extends Packet(379)
