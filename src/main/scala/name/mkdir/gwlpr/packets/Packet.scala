package name.mkdir.gwlpr.packets

import java.nio.ByteBuffer

// XXX - turn this into a trait with "def header: Short" ?
abstract class Packet(val header: Short) {
    def toBytes : Array[Byte]
    def size: Int
}
case class PacketError(h: Short, source: String) extends Packet(h) {
    def toBytes : Array[Byte] = null
    def size = 0
}

// XXX - Rename this.
trait Deserialiser {
    def apply(buf: ByteBuffer) : Packet
}

