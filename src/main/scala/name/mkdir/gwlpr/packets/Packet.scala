package name.mkdir.gwlpr.packets

import java.nio.ByteBuffer

abstract case class Packet(header: Short) {
    def toBytes : Array[Byte]
}
case class PacketError(h: Short, source: String) extends Packet(h) {
    def toBytes : Array[Byte] = null
}

// XXX - Rename this.
trait Deserialiser {
    def apply(buf: ByteBuffer) : Packet
}
