package name.mkdir.gwlpr.login

import name.mkdir.gwlpr.Packet
import name.mkdir.gwlpr.PacketAnnotations._

// C2S
case class ClientVersionPacket(data1: Int, clientVersion: Long, data3: Long, data4: Long) extends Packet(1024)

