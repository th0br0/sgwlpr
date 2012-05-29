package name.mkdir.gwlpr.packets

import java.nio.ByteBuffer

abstract case class Packet(header: Short)
case class PacketError(h: Short, source: String) extends Packet(h)

trait Deserialiser {
    def apply(buf: ByteBuffer) : Packet
}
