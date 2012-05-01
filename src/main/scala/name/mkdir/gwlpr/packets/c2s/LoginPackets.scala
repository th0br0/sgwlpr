package name.mkdir.gwlpr.packets.c2s

import name.mkdir.gwlpr.Config

import name.mkdir.gwlpr.packets.PacketAnnotations._
import name.mkdir.gwlpr.packets.Packet

case class KeepAlivePacket(data1: Long) extends Packet {
    def header = 0
}

case class ComputerInfoPacket(user: String, hostname: String) extends Packet{
    def header = 1
}

case class AccountLoginPacket(loginCount: Long, @ArrayInfo(constSize = true, size = 24) password: Array[Byte], email: String, data2: String, charName: String) extends Packet {
    def header = 4
    def passwordString = new String(password, Config.charSet)
}

case class ClientIDPacket(data1: Long) extends Packet {
    def header = 35
}

case class ResponseRequestPacket(loginCount: Long) extends Packet {
    def header = 53
}
