package name.mkdir.gwlpr.login.c2s

import name.mkdir.gwlpr.Config

import name.mkdir.gwlpr.PacketAnnotations._
import name.mkdir.gwlpr.Packet

case class KeepAlivePacket(data1: Long) extends Packet(0) 

case class ComputerInfoPacket(user: String, hostname: String) extends Packet(1) 

case class AccountLoginPacket(loginCount: Long, @ArrayInfo(constSize = true, size = 24) password: Array[Byte], email: String, data2: String, charName: String) extends Packet(4) {
    lazy val passwordString = 
      // maxSize is < 0xFF anyway ;S
      new String(password.drop(2).take(password(0)), Config.charSet)
}

case class LogoutPacket(data1: Long) extends Packet(13)
case class ComputerHardwarePacket(data1: Array[Byte], @ArrayInfo(constSize = true, size = 16) data2: Array[Byte]) extends Packet(15)

case class ClientIDPacket(data1: Long) extends Packet(35)

case class ResponseRequestPacket(loginCount: Long) extends Packet(53)






// --------------- UNKNOWN PACKETS ----------------
case class Packet19(data1: Long, data2: Long) extends Packet(19)
