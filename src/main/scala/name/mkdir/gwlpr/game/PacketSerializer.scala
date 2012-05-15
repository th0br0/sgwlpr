package name.mkdir.gwlpr.game

import akka.serialization._
import akka.util.ByteString
import java.nio.{ByteBuffer, ByteOrder}

import name.mkdir.gwlpr._

class PacketSerializer extends akka.serialization.Serializer {
    def includeManifest: Boolean = false
    def identifier = 1203981

    def toBinary(obj: AnyRef): Array[Byte] = PacketParser.unapply(obj.asInstanceOf[Packet])

    def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN) // should this be little endian???
        var packets : List[Packet] = Nil
        while(bb.hasRemaining)  {
          packets = (bb.getShort() match {

            // These are unencrypted
            case 1280 => PacketParser(classOf[ClientVerificationPacket], bb)
            case 16896 => PacketParser(classOf[ClientSeedPacket], bb)


            case _ => PacketError
          }) :: packets
        }
        packets.reverse.asInstanceOf[AnyRef]
      }
}
