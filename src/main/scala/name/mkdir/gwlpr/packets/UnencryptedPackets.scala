package name.mkdir.gwlpr.packets 

import name.mkdir.gwlpr.packets.PacketAnnotations._

case class ClientVersionPacket(data1: Int, clientVersion: Long, data3: Long, data4: Long) extends Packet {
    def header = 1024
}

case class ClientSeedPacket(@ArrayInfo(constSize=true, size=64) seed: Array[Byte]) extends Packet{
    def header = 16869
}
