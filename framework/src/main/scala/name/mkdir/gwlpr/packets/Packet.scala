package name.mkdir.gwlpr.packets

import java.nio.ByteBuffer
import name.mkdir.gwlpr.events.ClientMessageEvent
import name.mkdir.gwlpr.Session

// XXX - turn this into a trait with "def header: Short" ?
abstract class Packet(val header: Short) {
  def toBytes : Array[Byte]

  // XXX - this is not good
  def toEvent(session: Session): ClientMessageEvent

  def size: Int
}

case class PacketError(h: Short, source: String) extends Packet(h) {
  def toBytes : Array[Byte] = null
  def toEvent(session: Session) = null
  def size = 0
}

// XXX - Rename this.
trait Deserialiser {
  def apply(buf: ByteBuffer) : Packet
}

