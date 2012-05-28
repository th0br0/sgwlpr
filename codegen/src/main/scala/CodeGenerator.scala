package name.mkdir.codegen

import org.clapper.scalasti._
import java.io.File

object CodeGenerator {
  val stdir = new StringTemplateGroup("tpls", new File("templates"))

  def generate(packet: Packet, packageName: String, direction: String): String = {
    val classTemplate = stdir.template("class")

    classTemplate.setAttributes(Map(
      "package" -> packageName,
      "dir" -> direction,
      "nestedClasses" -> generateNestedClasses(packet.fields.filter(_.isInstanceOf[NestedField]).asInstanceOf[List[NestedField]]), // XXX - this is a nasty hack
      "attributes" -> generateAttributes(packet.fields),
      "assertions" -> generateAssertions(packet.fields.filter(_.arrayInfo != None)),
      "serialise" -> generateSerialisationFunction(packet),
      "className" -> packet.name,
      "size" -> packet.size,
      "header" -> packet.header))
    classTemplate.toString
  }

  def generateNestedClasses(fields: List[NestedField]): List[String] = {
    var nestedCount = -1
    def currentNested = { nestedCount += 1; nestedCount }

    fields.map { field =>
      val template = stdir.template("nested")
      template.setAttribute("className", "NestedPacket" + currentNested)
      template.setAttribute("attributes", generateAttributes(field.members))
      template.toString
    }
  }

  def generateAssertions(fields: List[PacketField]): List[String] = {
    def assertion(fieldName: String, arrayInfo: ArrayInfo): String = {
      val template = stdir.template("assertion")
      template.setAttribute("condition",
        fieldName + ".length" +
          {
            if (arrayInfo.fixedLength) " == "
            else " <= "
          } + arrayInfo.length)
      template.toString
    }

    fields.map {
      case f: PacketField => assertion(f.info.name.get, f.arrayInfo.get)
    }
  }

  def generateAttributes(fields: List[PacketField]): List[String] = {
    var nestedCount = -1
    def currentNested = { nestedCount += 1; nestedCount }

    (fields.map {
      case field @ Field(fieldType, info, arrayInfo) =>
        val attributeTemplate = stdir.template("attribute")
        attributeTemplate.setAttribute("name", info.name.get)

        attributeTemplate.setAttribute("type", {
          if (arrayInfo == None || (fieldType == Utf16 || fieldType == Ascii))
            field.typeMapping
          else
            "List[" + field.typeMapping + "]"
        })
        attributeTemplate.toString
      case NestedField(info, _, arrayInfo) =>
        val attributeTemplate = stdir.template("attribute")
        attributeTemplate.setAttribute("name", info.name.get)
        attributeTemplate.setAttribute("type", {
          if (arrayInfo == None)
            "NestedPacket" + currentNested
          else
            "List[NestedPacket" + currentNested + "]"
        })
        attributeTemplate.toString
    })
  }

  def generateSerialisationFunction(packet: Packet): String = {
    val serialisationTemplate = stdir.template("serialise")

    serialisationTemplate.setAttribute("content",
      packet.fields.map {
        case f: NestedField => serialiseNested(f)
        case f: Field => serialiseField(f)
      })

    serialisationTemplate.toString
  }

  def serialiseNested(field: NestedField): String = {
    if (field.arrayInfo == None)
      field.members.foldRight("") { (b, a) => serialiseFieldType(b.fieldType).format(b.info.name) + a }
    else {
      val inner = field.members.foldRight("") { (b, a) => serialiseFieldType(b.fieldType).format("i." + b.info.name) + a }

      {
        val ai = field.arrayInfo.get
        if (!ai.fixedLength)
          serialiseFieldType(ai.prefixType).format(field.info.name.get + ".length")
        else ""
      } + "%s.foreach { i => %s }\n".format(field.info.name.get, inner)
    }
  }

  def serialiseFieldType(fieldType: FieldType): String = (fieldType match {
    case Int8 => "buf.putByte(%s)"
    case Int16 => "buf.putShort(%s)"
    case Int32 => "buf.putInt(%s)"
    case Int64 => "buf.putLong(%s)"
    case Float => "buf.putFloat(%s)"
    case Vec2 => "// XXX - Implement Vector2 deserialisation"
    case Vec3 => "// XXX - Implement Vector3 deserialisation"
    case Vec4 => "// XXX - Implement Vector4 deserialisation"
    case Uuid16 => "// XXX - Implement Uuid16 deserialisation"
    case Uuid28 => "// XXX - Implement Uuid28 deserialisation"
    case AgentId => "buf.putInt(%s) // XXX - Implement AgentId"
    case Utf16 => """buf.put(%s.getBytes("UTF-16LE"))"""
    case _ => "// XXX - Unknown field serialised: %s"
  }) + "\n"

  def serialiseField(field: Field): String = {
    if (field.arrayInfo == None)
      serialiseFieldType(field.fieldType).format(field.info.name.get)
    else {
      val ai = field.arrayInfo.get
      val cmd = serialiseFieldType(field.fieldType)

      {
        if (!ai.fixedLength)
          serialiseFieldType(ai.prefixType).format(field.info.name.get + ".length")
        else ""
      } +
        {
          if (field.fieldType == Utf16)
            cmd.format(field.info.name.get)
          else
            "%s.foreach { i => %s }\n".format(field.info.name.get, cmd.format("i"))
        }
    }
  }
}
