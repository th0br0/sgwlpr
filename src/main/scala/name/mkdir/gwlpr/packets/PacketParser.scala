package name.mkdir.gwlpr.packets

import java.nio.ByteBuffer

abstract sealed trait DataType

case object UInt16 extends DataType
case object UInt32 extends DataType
case object Byte extends DataType
case object Float extends DataType

case class String(length: Int) extends DataType
case class ByteArray(constSize: Boolean, length: Int) extends DataType
case class UInt16Array(constSize: Boolean, length: Int) extends DataType
case class UInt32Array(constSize: Boolean, length: Int) extends DataType

object PacketParser {
    val byteMask: Short = 0xFF
    val shortMask: Short = (0xFFFF).toShort
    val charSet = "UTF16-LE"

    val BYTE = classOf[Byte]
    val UINT16 = classOf[Int]
    val UINT32 = classOf[Long]
    val STRING = classOf[String]
    val FLOAT = classOf[Float]
    val BYTEARRAY = classOf[Array[Byte]]
    val INTARRAY = classOf[Array[Int]]
    val LONGARRAY = classOf[Array[Long]]
    private def readByte(buf: ByteBuffer): Byte = (buf.get() & byteMask).toByte
    private def readUInt16(buf: ByteBuffer): Int = (buf.getShort() & shortMask)
    private def readUInt32(buf: ByteBuffer): Long = (buf.getInt() & 0xFFFFFFFFL)
    private def readFloat(buf: ByteBuffer): Float = buf.getFloat()
    def apply(clazz: Class[_ <: Packet], buf: ByteBuffer) = {

      val args = clazz.getDeclaredFields().map { case f => (f.getGenericType() match {
            case BYTE => readByte(buf)
            case UINT16 => readUInt16(buf)
            case UINT32 => readUInt32(buf)
            case FLOAT => readFloat(buf)

            case STRING => {
                val length = readUInt16(buf)
                val limit = buf.limit()
                val ret = {
                    if(length - 1 > 0) {
                        new java.lang.String(buf.limit(buf.position() + length * 2).asInstanceOf[ByteBuffer].compact().array(), charSet)
                    } else
                    "" //TODO: FIXME?
                }
                buf.limit(limit)
                ret
            }

            case BYTEARRAY => {
                val info = f.getDeclaredAnnotations()(0).asInstanceOf[PacketArray] // this might crash but I don't care.
                val limit = buf.limit()
                println("pre byte array: " + buf)
                val ret = {
                    if(info.constSize)
                        buf.limit(buf.position() + info.size).asInstanceOf[ByteBuffer].compact().array()
                    else
                        buf.limit(buf.position() + readUInt16(buf)).asInstanceOf[ByteBuffer].compact().array()
                }
                buf.limit(limit)
                println("Read byte array: " + buf)
                ret
            }

            case INTARRAY => throw new RuntimeException("implement me")
            case LONGARRAY => throw new RuntimeException("implement me")
        }).asInstanceOf[Object]
      } 

      clazz.getConstructors()(0).newInstance(args:_*)
    }
}
