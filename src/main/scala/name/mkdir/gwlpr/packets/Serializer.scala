package name.mkdir.gwlpr.packets

import akka.serialization._
import akka.util.ByteString
import java.nio.{ByteBuffer, ByteOrder}
import annotation.target.field

object PacketAnnotations {
    type ArrayInfo = name.mkdir.gwlpr.packets.PacketArray @field
}

trait Packet { 
    def header: Int
}

class Serializer extends akka.serialization.Serializer {
    def includeManifest: Boolean = false
    def identifier = 1203981

    def toBinary(obj: AnyRef): Array[Byte] = 
        Array[Byte](0) //TODO: implement this

    def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN) // should this be little endian???
        println(bb.getShort() match {
            case 1024 => PacketParser(classOf[ClientVersionPacket], bb)
            case 16896 => PacketParser(classOf[ClientSeedPacket], bb)
        })



        ""
      }
}
