package name.mkdir.gwlpr.login

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

            case 0 => PacketParser(classOf[KeepAlivePacket], bb)
            case 1 => PacketParser(classOf[ComputerInfoPacket], bb)
            case 4 => PacketParser(classOf[AccountLoginPacket], bb)
            case 13 => PacketParser(classOf[LogoutPacket], bb)
            case 14 => PacketParser(classOf[ExitPacket], bb)
            case 15 => PacketParser(classOf[ComputerHardwarePacket], bb)
            case 19 => PacketParser(classOf[Packet19], bb)
            case 35 => PacketParser(classOf[ClientIDPacket], bb)
            case 41 => PacketParser(classOf[CharacterPlayPacket], bb)
            case 53 => PacketParser(classOf[ResponseRequestPacket], bb)
            // These are unencrypted
            case 1024 => PacketParser(classOf[ClientVersionPacket], bb)
            case 16896 => PacketParser(classOf[ClientSeedPacket], bb)


            case _ => PacketError
          }) :: packets
        }
        packets.reverse.asInstanceOf[AnyRef]
      }
}
