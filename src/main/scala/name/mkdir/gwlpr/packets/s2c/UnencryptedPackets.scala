package name.mkdir.gwlpr.packets.s2c

import name.mkdir.gwlpr.packets.Packet
import name.mkdir.gwlpr.packets.PacketAnnotations._

case class ServerSeedPacket(@ArrayInfo(constSize = true, size = 20) seed: Array[Byte]) extends Packet{
    def header = 5633
}
