package name.mkdir.gwlpr.login

import name.mkdir.gwlpr.Packet
import name.mkdir.gwlpr.PacketAnnotations._

// C2S
case class ClientVersionPacket(data1: Int, clientVersion: Long, data3: Long, data4: Long) extends Packet(1024)

case class ClientSeedPacket(@ArrayInfo(constSize=true, size=64) seed: Array[Byte]) extends Packet(16869)

// S2C
case class ServerSeedPacket(@ArrayInfo(constSize = true, size = 20) seed: Array[Byte]) extends Packet(5633)
