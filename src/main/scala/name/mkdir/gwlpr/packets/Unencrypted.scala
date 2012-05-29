package name.mkdir.gwlpr.packets.unenc

import name.mkdir.gwlpr.packets._

import java.nio.{ByteBuffer, ByteOrder}

sealed abstract trait SeedPacketTrait {
    def seed: List[Byte]
    def header: Short

    assert(seed.length == 64)

    def toBytes: Array[Byte] = {
        val buf = ByteBuffer.allocate(2 + 64).order(ByteOrder.LITTLE_ENDIAN)
        buf.putShort(header)
        buf.put(seed.toArray)
        buf.array()
    }
}

class ClientSeedPacket(val seed: List[Byte]) extends Packet(16869) with SeedPacketTrait
class ServerSeedPacket(val seed: List[Byte]) extends Packet(5633) with SeedPacketTrait


sealed trait InboundPacket { 
    def toBytes: Array[Byte] = null // this should never get called anyway
}
class ClientVersionPacket(val unknown0: Short, val clientVersion: Int, val unknown1: Int, val unknown2: Int) extends Packet(0x400) with InboundPacket
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
  val unknown5: Int) extends Packet(0x500) with InboundPacket

object Deserialise extends Deserialiser {
   def apply(buf: ByteBuffer) : Packet = {
        def getBytes(length: Int) = {
            val arr = new Array[Byte](length)
            buf.get(arr)
            arr.toList
        }
        val header = buf.getShort

        header match {
            case 16869 => new ClientSeedPacket(getBytes(64))
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

        }
   }
}
