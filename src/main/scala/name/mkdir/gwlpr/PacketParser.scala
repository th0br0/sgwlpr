package name.mkdir.gwlpr

import java.nio.ByteBuffer
import name.mkdir.gwlpr.Config

import annotation.target.field

object PacketAnnotations {
    type ArrayInfo = name.mkdir.gwlpr.PacketArray @field
}

abstract class Packet(val header: Short)

case object PacketError extends Packet(-1)

sealed abstract trait LoginPacket
sealed abstract trait GamePacket

object PacketParser {
    val byteMask: Short = 0xFF
    val shortMask: Short = (0xFFFF).toShort

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
    def apply[T <: Packet](clazz: Class[T], buf: ByteBuffer) : T = {

      val args = clazz.getDeclaredFields().filter(_.getModifiers() == 18).map { case f => (f.getGenericType() match {
            case BYTE => readByte(buf)
            case UINT16 => readUInt16(buf)
            case UINT32 => readUInt32(buf)
            case FLOAT => readFloat(buf)

            case STRING => {
                val length = readUInt16(buf)
                val arr = new Array[Byte](length * 2)
                buf.get(arr)
                new String(arr, Config.charSet)
            }

            case BYTEARRAY => {
                val ann = f.getDeclaredAnnotations()
                lazy val info = ann(0).asInstanceOf[PacketArray] 
                val limit = buf.limit()
                
                val length = {if(ann.length > 0 && info.constSize) info.size else readUInt16(buf)}
                val arr = new Array[Byte](length)
                buf.get(arr)

                arr
            }

            case INTARRAY => throw new RuntimeException("implement me")
            case LONGARRAY => throw new RuntimeException("implement me")
        }).asInstanceOf[Object]
      } 
      clazz.getConstructors()(0).newInstance(args:_*).asInstanceOf[T]
    }

    def unapply[T <: Packet](obj: T): Array[Byte] = {
        import java.nio.ByteOrder

        val clazz = obj.getClass()
        //Calculate the length...
        //Optimise this. We should only be calling f.get once for each field...
        val length = ((clazz.getDeclaredFields().map { case f => f.getGenericType() match {
            case BYTE => 1
            case UINT16 => 2
            case UINT32 => 4
            case FLOAT => 4
            case STRING => {
                f.setAccessible(true)
                val str = f.get(obj).asInstanceOf[String]
                str.length * 2 + 2
            }
            case BYTEARRAY => {
                f.setAccessible(true)
                val barr = f.get(obj).asInstanceOf[Array[Byte]]
                val anno = f.getDeclaredAnnotations()
                if(anno.length == 0)
                    barr.length + 2
                else
                    barr.length
            }
            case INTARRAY => throw new RuntimeException("implement me"); 0
            case LONGARRAY => throw new RuntimeException("implement me"); 0
          }
        }) sum) + 2 // + 2 due to header

        val buf = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN)
        buf.putShort(obj.header)
        clazz.getDeclaredFields().map { case f => f.getGenericType() match {
            case BYTE => f.setAccessible(true); buf.put(f.get(obj).asInstanceOf[Byte])
            case UINT16 => f.setAccessible(true); buf.putShort(f.get(obj).asInstanceOf[Int].toShort)
            case UINT32 => f.setAccessible(true); buf.putInt(f.get(obj).asInstanceOf[Long].toInt)
            case FLOAT => f.setAccessible(true); buf.putFloat(f.get(obj).asInstanceOf[Float])
            case STRING => {
              f.setAccessible(true); 
                val str = f.get(obj).asInstanceOf[String]
                buf.putShort(str.length.toShort)
                buf.put(str.getBytes(Config.charSet)) 
            }
            case BYTEARRAY => {
              f.setAccessible(true); 
              
              val barr = f.get(obj).asInstanceOf[Array[Byte]]
              val anno = f.getDeclaredAnnotations()
              
              if(anno.length == 0 || !anno(0).asInstanceOf[PacketArray].constSize) {
                buf.putShort(barr.size.toShort)
                buf.put(barr)
              } else
                buf.put(barr)
            }
            case INTARRAY => throw new RuntimeException("implement me"); 0
            case LONGARRAY => throw new RuntimeException("implement me"); 0
          }
        } 
        buf.array()
      }
}
