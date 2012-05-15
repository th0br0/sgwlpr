package name.mkdir.gwlpr

import PacketAnnotations._

// C2S
case class ClientSeedPacket(@ArrayInfo(constSize=true, size=64) seed: Array[Byte]) extends Packet(16869)

// S2C
case class ServerSeedPacket(@ArrayInfo(constSize = true, size = 20) seed: Array[Byte]) extends Packet(5633)
