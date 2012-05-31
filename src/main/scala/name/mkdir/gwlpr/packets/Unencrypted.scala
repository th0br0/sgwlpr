package name.mkdir.gwlpr.packets.unenc

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.Session
import name.mkdir.gwlpr.events.ClientMessageEvent

import java.nio.{ByteBuffer, ByteOrder}


sealed abstract trait SeedPacketTrait {
    def seedSize : Int
    def seed: List[Byte]
    def header: Short
    def size: Int = 2 + seedSize

    assert(seed.length <= seedSize)

    def toBytes: Array[Byte] = {
        val buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        buf.putShort(header)
        buf.put(seed.toArray)
        buf.array()
    }
}

case class ClientSeedPacketEvent(session: Session, packet: ClientSeedPacket) extends ClientMessageEvent
class ClientSeedPacket(val seed: List[Byte]) extends Packet(16896) with SeedPacketTrait {
    def toEvent(session: Session) = ClientSeedPacketEvent(session, this)
    def seedSize = 64
}
class ServerSeedPacket(val seed: List[Byte]) extends Packet(5633) with SeedPacketTrait {
    def toEvent(session: Session) = null
    def seedSize = 20
}



sealed trait InboundPacket { 
    def toBytes: Array[Byte] = null // this should never get called anyway
}

case class ClientVersionPacketEvent(session: Session, packet: ClientVersionPacket) extends ClientMessageEvent
class ClientVersionPacket(val unknown0: Short, val clientVersion: Int, val unknown1: Int, val unknown2: Int) extends Packet(0x400) with InboundPacket {
    def size = 2 + 4 + 4 + 4
    def toEvent(session: Session) = ClientVersionPacketEvent(session, this)
}


case class ClientVerificationPacketEvent(session: Session, packet: ClientVerificationPacket) extends ClientMessageEvent
class ClientVerificationPacket(
  val unknown0: Short,
  val unknown1: Int,
  val unknown2: Int,
  val securityKey1: List[Byte],
  val unknown3: Int,
  val securityKey2: List[Byte],
  val accountHash: List[Byte],
  val characterHash: List[Byte],
  val unknown4: Int,
  val unknown5: Int) extends Packet(0x500) with InboundPacket {
    
  def size = 2 + 4 + 4 + 4 + 4 + 4 + 16 + 16 + 4 + 4
  def toEvent(session: Session) = ClientVerificationPacketEvent(session, this)

}

object Deserialise extends Deserialiser {
   def apply(buf: ByteBuffer) : Packet = {
        def getBytes(length: Int) = {
            val arr = new Array[Byte](length)
            buf.get(arr)
            arr.toList
        }
        val header = buf.getShort

        header match {
            case 16896 => new ClientSeedPacket(getBytes(64))
            case 0x400 => 
                new ClientVersionPacket(
                    buf.getShort,
                    buf.getInt,
                    buf.getInt,
                    buf.getInt
                )
            case 0x500 => new ClientVerificationPacket(
                    buf.getShort,
                    buf.getInt,
                    buf.getInt,
                    getBytes(4),
                    buf.getInt,
                    getBytes(4),
                    getBytes(16),
                    getBytes(16),
                    buf.getInt,
                    buf.getInt
                  )
            case s: Short => new PacketError(s, "unencrypted")

        }
   }
}
