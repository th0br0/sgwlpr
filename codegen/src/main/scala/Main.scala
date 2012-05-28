package name.mkdir.codegen

import scala.xml._
import scala.collection._

// What I'd give for autoproxy... 
sealed abstract class FieldType(val typeMapping: String, val size: Int)
case object Int8 extends FieldType("Byte", 1)
case object Int16 extends FieldType("Short", 2)
case object Int32 extends FieldType("Int", 4)
case object Int64 extends FieldType("Long", 8)
case object Float extends FieldType("Float", 4)
case object Vec2 extends FieldType("Vector2", 2*4) 
case object Vec3 extends FieldType("Vector3", 3*4)
case object Vec4 extends FieldType("Vector4", 4*4)
case object Uuid16 extends FieldType("?uuid16", 16) // XXX - What is the size of a uuid?
case object Uuid28 extends FieldType("?uuid28", 28)
case object AgentId extends FieldType("Int", 4) // XXX - this should be AgentId and use implicits
case object Ascii extends FieldType("String", 2) // XXX - is this right?
case object Utf16 extends FieldType("String", 2) // XXX - should be right?
case object Packed extends FieldType("?packed", -65535) // XXX - FixMe.
case object Nested extends FieldType("ERROR", -1)

case class ArrayInfo(length: Int, fixedLength: Boolean, prefixType: FieldType)

abstract sealed trait PacketField {
    def info: Info
    def arrayInfo: Option[ArrayInfo]
    def size: Int
}
case class Field(fieldType: FieldType, info: Info, arrayInfo: Option[ArrayInfo] = None) extends PacketField {
    def typeMapping = fieldType.typeMapping
    def size = {
        if(arrayInfo == None)
          fieldType.size
        else {
            val ai = arrayInfo.get
            if(ai.fixedLength)
              ai.length * fieldType.size
            else
              ai.prefixType.size + ai.length * fieldType.size
        }
    }
}
case class NestedField(info: Info, members: List[Field], arrayInfo: Option[ArrayInfo] = None) extends PacketField {
    private def memberSize = members.foldLeft(0){ case (a,b) => a + b.size }
    def size = {
      if(arrayInfo == None)
        memberSize
      else {
        val ai = arrayInfo.get
        if(ai.fixedLength)
          ai.length * memberSize
        else
          ai.prefixType.size + ai.length * memberSize
      }
    }
}


case class Packet(header: Int, fields: List[PacketField], info: Info) {
    def name : String = info.name match {
        case None => "Packet%d".format(header)
        case Some(name) => name
    }
    def size = fields.foldLeft(0) { case (a,b) => a + b.size }
}

// XXX - name should not be var
case class Info(var name: Option[String], description: Option[String], author: Option[String])

object Main extends App {
    override def main(args: Array[String]) : Unit = {
        val target = "src-gen"
        val packageName = "name.mkdir.gwlpr.packets"
        val fileName = "PacketTemplates.xml"

        val packetMap = mutable.Map.empty[String, List[Packet]]

        (XML.loadFile(fileName) \\ "Packets" \ "Direction").toList.foreach{ direction => 
            val dir = (direction \ "@abbr").text match {
                case "LStoC" => "l2c"
                case "GStoC" => "g2c"
                case "CtoLS" => "c2l"
                case "CtoGS" => "c2g"
                case _ => "error"
            }
            val packets = (direction \ "Packet").toList

            packetMap += (dir -> packets.map( deserializePacket ).toList)
        }

        println(CodeGenerator.generate(packetMap("g2c")(234), packageName, "g2c"))
      }

      implicit def string2Option(str: String) : Option[String] = {
        if(str.isEmpty)
          None
        else
          Some(str)
      }

      implicit def string2FieldType(str: String) : FieldType = str match {
            case "int8" => Int8
            case "int16" => Int16
            case "int32" => Int32
            case "int64" => Int64
            case "packed" => Packed
            case "float" => Float
            case "vec2" => Vec2
            case "vec3" => Vec3
            case "vec4" => Vec4
            case "uuid16" => Uuid16
            case "uuid28" => Uuid28
            case "agentid" => AgentId
            case "ascii" => Ascii
            case "utf16" => Utf16
            case "nested" => Nested
      }

      def deserializeInfo(info: NodeSeq) : Info = Info(
        (info \ "Name").text,
        (info \ "Description").text,
        (info \ "Author").text
      )

      def deserializePacket(packet: NodeSeq) : Packet = {
        val header = (packet \ "@header").text.toInt
        val info = deserializeInfo(packet \ "Info")

        val fields = deserializeFields(packet \ "Field")
        Packet(header, fields, info)
      }

    def deserializeFields(fields: NodeSeq) : List[PacketField] = {
        var unknownCount = 0
        fields.map(deserializeField).toList.map{ field =>
            if(field.info.name == None) {
                field.info.name = Some("unknown" + unknownCount)
                unknownCount += 1
            }

            field
        }
    }

    def deserializeField(field: NodeSeq) : PacketField = {
          val fieldType : FieldType = (field \ "@type").text 

          val info = deserializeInfo(field \ "Info")
          val array : Option[ArrayInfo] = {
                val occursStr = (field \ "@occurs").text
                if(occursStr.isEmpty)
                  None
                else {
                    val occurs = occursStr.toInt
                    val static = (field \ "@static").text match {
                      case "false" => false
                      case _ => true
                    }
                    val prefixType : FieldType = (field \ "@prefixType").text

                    Some(ArrayInfo(occurs, static, prefixType))
                }
            }

          if(fieldType == Nested) {
            val fields = deserializeFields(field \ "Field")
            NestedField(info, fields.asInstanceOf[List[Field]], array) // XXX - Fix this zZz hack
          }
          else 
            Field(fieldType, info, array)
        
    }
}
