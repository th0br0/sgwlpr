package name.mkdir.gwlpr.login.c2s

import name.mkdir.gwlpr.PacketAnnotations._
import name.mkdir.gwlpr.Packet

case class ComputerInfoReply(staticData: Long = 0x71953D3D, loginCount: Long, data3: Long = 0, data4: Long = 1) extends Packet{
    def header = 1
}

case class StreamTerminatorPacket(loginCount: Long, errorCode: Long = 0) extends Packet {
    def header = 3
}

case class ResponseRequestReply(loginCount: Long, data1: Long = 0) extends Packet {
    def header = 38
}

