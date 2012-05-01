package name.mkdir.gwlpr.login.s2c

import name.mkdir.gwlpr.Packet
import name.mkdir.gwlpr.PacketAnnotations._

case class ServerSeedPacket(@ArrayInfo(constSize = true, size = 20) seed: Array[Byte]) extends Packet{
    def header = 5633
}
