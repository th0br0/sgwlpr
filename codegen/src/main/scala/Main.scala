package name.mkdir.codegen

import scala.xml._
import scala.collection._
import java.io.{ File, BufferedWriter, FileWriter }
import FieldTypes._
// What I'd give for autoproxy... 
object FieldTypes {
  sealed abstract class FieldType(val typeMapping: String, val size: Int)
  case object Int8 extends FieldType("Byte", 1)
  case object Int16 extends FieldType("Short", 2)
  case object Int32 extends FieldType("Int", 4)
  case object Int64 extends FieldType("Long", 8)
  case object Float extends FieldType("Float", 4)
  case object Vec2 extends FieldType("Vector2", 2 * 4)
  case object Vec3 extends FieldType("Vector3", 3 * 4)
  case object Vec4 extends FieldType("Vector4", 4 * 4)
  case object Uuid16 extends FieldType("?uuid16", 16) // XXX - What is the size of a uuid?
  case object Uuid28 extends FieldType("?uuid28", 28)
  case object AgentId extends FieldType("AgentId", 4) // XXX - this should be AgentId and use implicits
  case object Ascii extends FieldType("String", 2) // XXX - is this right?
  case object Utf16 extends FieldType("String", 2) // XXX - should be right?
  case object Packed extends FieldType("?packed", -65535) // XXX - FixMe.
  case object Nested extends FieldType("ERROR", -1)
}

/**
 * Internal representation of the array information associated with a packet
 * @param length the length of the array
 * @param fixedLength is the length constant
 * @param prefixType the [[name.mkdir.gwlpr.codegen.FieldType]] of the prefix that needs to be serialised if the length is not constant
 */
case class ArrayInfo(length: Int, fixedLength: Boolean, prefixType: FieldType)

/**
 * Base PacketField trait
 */
abstract sealed trait PacketField {
  def info: Info
  def arrayInfo: Option[ArrayInfo]
  def size: Int

  /**
   * Calculates the size of a packet taking available [[name.mkdir.gwlpr.codegen.ArrayInfo]] into account
   *
   * @param size the base packet size that is used for calculations
   */
  protected def realSize(size: Int) = {
    if (arrayInfo == None)
      size
    else {
      val ai = arrayInfo.get
      if (ai.fixedLength)
        ai.length * size
      else
        // XXX - Should the size really be independent of the list's length
        ai.prefixType.size + ai.length * size
    }

  }
}

/**
 * A normal packet field
 *
 * @param fieldType the type for this field
 * @param info the info metadata for this field
 * @param arrayInfo the array metadata for this field
 */
case class Field(fieldType: FieldType, info: Info, arrayInfo: Option[ArrayInfo] = None) extends PacketField {
  def typeMapping = fieldType.typeMapping
  def size = realSize(fieldType.size)
}

/**
 * A nested packet field
 *
 * @param info the info metadata for this field
 * @param members a list of the nested fields
 * @param arrayInfo the array metadata for this field
 */
case class NestedField(info: Info, members: List[Field], arrayInfo: Option[ArrayInfo] = None) extends PacketField {
  private def memberSize = members.foldLeft(0) { case (a, b) => a + b.size }
  def size = realSize(memberSize)
}

/**
 * Internal representation of a <Packet /> node
 *
 * @param header the packet header
 * @param fields the list of fields contained in this packet
 * @param info the metadata associated with this packet
 */
case class Packet(header: Int, fields: List[PacketField], info: Info) {
  /** Creates a default packet name if none is supplied by the metadata */
  def name: String = info.name match {
        case None => "Packet%d".format(header)
        case Some(name) => name + "Packet"
  }

  /** Adds up the sizes of the internal fields */
  def size = 2 + fields.foldLeft(0) { case (a, b) => a + b.size }
}

// XXX - name should not be var
/**
 * Internal representation of packet and field metadata
 *
 * @param name the name for the packet/field
 * @param description the description for the packet/field
 * @param author the author for the packet/field
 */
case class Info(var name: Option[String], description: Option[String], author: Option[String], value: Option[String])

object Main extends App {

  /** Generates scala code from the packet template xml */
  override def main(args: Array[String]): Unit = {
    // XXX - get these as config options from args
    val target = "src-gen"
    val packageName = "name.mkdir.gwlpr"
    val fileName = "PacketTemplates.xml"

    val packetMap = mutable.Map.empty[String, List[Packet]]

    // parse XML to packets
    (XML.loadFile(fileName) \\ "Packets" \ "Direction").toList.foreach { direction =>
      // convert the verbose abbreviations to less verbose ones
      val dir = (direction \ "@abbr").text match {
        case "LStoC" => "l2c"
        case "GStoC" => "g2c"
        case "CtoLS" => "c2l"
        case "CtoGS" => "c2g"
        case _ => "error"
      }
      val packets = (direction \ "Packet").toList

      packetMap += (dir -> packets.map(deserialisePacket).toList)
    }

    // save packets as scala classes
    packetMap.toList.foreach {
      case (d, p) => {
        import org.clapper.scalasti.StringTemplateGroup

        val f = new File(target + "/" + d + ".scala")
        f.delete
        f.createNewFile

        val bw = new BufferedWriter(new FileWriter(f))

        val tpl = new StringTemplateGroup("", new File("templates")).template("base")
        tpl.setAttribute("package", packageName)
        tpl.setAttribute("dir", d)
        tpl.setAttribute("content", p.foldLeft("") { (a, b) => a + "\n" + CodeGenerator.generate(b) })

        val des = new StringTemplateGroup("", new File("templates")).template("deserialiser")
        des.setAttribute("cases", p.map { packet =>
          "case %d => %s(buf)\n".format(packet.header, packet.name)
        })

        bw.write(tpl.toString)
        bw.write(des.toString)
        bw.flush
        bw.close
      }
    }

  }

  /** This method implicitly converts a String to an Option[String] */
  implicit def string2Option(str: String): Option[String] = {
    // XXX - Is there no Scala internal for this? 
    if (str.isEmpty)
      None
    else
      Some(str)
  }

  /** This method implicitly converts a String to the representative FieldType */
  implicit def string2FieldType(str: String): FieldType = str match {
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

  /** Parses the <Info /> node */
  def deserialiseInfo(info: NodeSeq): Info = Info(
    (info \ "Name").text,
    (info \ "Description").text,
    (info \ "Author").text,
    (info \ "Value").text)

  /* Parses the <Packet /> node */
  def deserialisePacket(packet: NodeSeq): Packet = {
    val header = (packet \ "@header").text.toInt
    val info = deserialiseInfo(packet \ "Info")

    val fields = deserialiseFields(packet \ "Field")
    Packet(header, fields, info)
  }

  /** Parses <Field /> nodes */
  def deserialiseFields(fields: NodeSeq): List[PacketField] = {
    var unknownCount = 0
    fields.map(deserialiseField).toList.map { field =>
      if (field.info.name == None) {
        field.info.name = Some("unknown" + unknownCount)
        unknownCount += 1
      }

      field
    }
  }

  /** Parses a <Field /> node */
  def deserialiseField(field: NodeSeq): PacketField = {
    val fieldType: FieldType = (field \ "@type").text

    val info = deserialiseInfo(field \ "Info")

    // Create the ArrayInfo for the field
    val array: Option[ArrayInfo] = {
      val occursStr = (field \ "@occurs").text
      if (occursStr.isEmpty)
        None
      else {
        val occurs = occursStr.toInt
        val static = (field \ "@static").text match {
          case "false" => false
          case _ => true
        }
        val prefixType: FieldType = (field \ "@prefixType").text

        Some(ArrayInfo(occurs, static, prefixType))
      }
    }

    // Special treatment for nested nodes
    if (fieldType == Nested) {
      val fields = deserialiseFields(field \ "Field")
      NestedField(info, fields.asInstanceOf[List[Field]], array) // XXX - Fix this zZz hack
    } else
      Field(fieldType, info, array)

  }
}
