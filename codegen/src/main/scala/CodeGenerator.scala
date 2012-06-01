package name.mkdir.codegen

import org.clapper.scalasti._
import java.io.File
import FieldTypes._

object CodeGenerator {
  val stdir = new StringTemplateGroup("tpls", new File("templates"))

  def generate(packet: Packet): String = {
    val classTemplate = stdir.template("class")

    classTemplate.setAttributes(Map(
      "nestedClasses" -> generateNestedClasses(packet),
      "attributes" -> generateAttributes(packet.fields, packet.name),
      "assertions" -> generateAssertions(packet.fields.filter(_.arrayInfo != None)),
      "serialise" -> generateSerialisationFunction(packet),
      "deserialise" -> generateDeserialisationFunction(packet),
      "className" -> packet.name,
      "size" -> generateSize(packet.fields),
      "header" -> packet.header))
    classTemplate.toString
  }

  def generateNestedClasses(packet: Packet): List[String] = {
    var nestedCount = -1
    def currentNested = { nestedCount += 1; nestedCount }

    packet.fields.filter(_.isInstanceOf[NestedField]).map {
      case field: NestedField =>
        val template = stdir.template("nested")
        template.setAttribute("className", "NestedPacket" + currentNested)
        template.setAttribute("attributes", generateAttributes(field.members, packet.name))
        template.toString
    }
  }

  def generateSize(fields: List[PacketField]) : String = {
    def gen(prefix: String, fields: List[PacketField]) : String = {
        val noarray = fields.filter(_.arrayInfo == None)
        val staticsize = {
            if(noarray.length > 0) noarray.map(_.size).reduceLeft(_ + _)
              else 0 }

        val arrays = fields.diff(noarray)
        val staticarrays = arrays.filter{f => f.arrayInfo.get.fixedLength && !(f.isInstanceOf[NestedField])}
        val staticsize2 = {
          if(staticarrays.length > 0) staticarrays.map(_.size).reduceLeft(_ + _)
        else 0 }

        val dynarrays = arrays.diff(staticarrays)
        val normalarrays = dynarrays.filterNot(_.isInstanceOf[NestedField])
        val normalstr = {
            if(normalarrays.length > 0) normalarrays.map { f => 
            val ai = f.arrayInfo.get
            "%d + (%s.length * %d)".format(ai.prefixType.size, ({if(!prefix.isEmpty) prefix + "." else ""}+ f.info.name.get), f.asInstanceOf[Field].fieldType.size)
        } reduceLeft(_ + " + " + _)
         else "" }


        if(!normalstr.isEmpty)
             "2 + %d + %d + %s".format(staticsize, staticsize2, normalstr)
        else
             "2 + %d + %d".format(staticsize, staticsize2)
            
    }
    val nested = fields.filter(_.isInstanceOf[NestedField])
    val normal = fields.diff(nested)

    gen("", normal) + {
        if(nested.length > 0) " + " + nested.map{n => 
            val single = gen(n.info.name.get, n.asInstanceOf[NestedField].members)
            if(n.arrayInfo == None)
                single
            else 
              n.info.name.get + ".map{ field => %s }.reduceLeft(_+_)".format(single.replace(n.info.name.get, "field"))
            }.reduceLeft(_ + " + " + _)
        else "" }
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

  def generateAttributes(fields: List[PacketField], name: String): List[String] = {
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

        if(info.value != None) {
            attributeTemplate.setAttribute("value", " = " + info.value.get)
        }
      
        attributeTemplate.toString
      case NestedField(info, _, arrayInfo) =>
        val attributeTemplate = stdir.template("attribute")
        attributeTemplate.setAttribute("name", info.name.get)
        attributeTemplate.setAttribute("type", {
          if (arrayInfo == None)
            "%s.NestedPacket%d".format(name, currentNested)
          else
            "List[%s.NestedPacket%d]".format(name, currentNested)
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
      field.members.foldRight("") { (b, a) => serialiseFieldType(b.fieldType).format(b.info.name.get) + a }
    else {
      val inner = field.members.foldRight("") { (b, a) => serialiseFieldType(b.fieldType).format("i." + b.info.name.get) + a }

      {
        val ai = field.arrayInfo.get
        if (!ai.fixedLength)
          serialiseFieldType(ai.prefixType).format(field.info.name.get + ".length")
        else ""
      } + "%s.foreach { i => %s }\n".format(field.info.name.get, inner)
    }
  }

  def serialiseFieldType(fieldType: FieldType): String = (fieldType match {
    case Int8 => "buf.put(%s.toByte)"
    case Int16 => "buf.putShort(%s)"
    case Int32 => "buf.putInt(%s)"
    case Int64 => "buf.putLong(%s)"
    case Float => "buf.putFloat(%s)"
    case Vec2 | Vec3 | Vec4 => "// XXX - Implement Vector* serialisation"
    case Uuid16 => "// XXX - Implement Uuid16 serialisation"
    case Uuid28 => "// XXX - Implement Uuid28 serialisation"
    case AgentId => "buf.putInt(%s) // XXX - Implement AgentId"
    case Utf16 => """buf.put(%s.getBytes("UTF-16LE"))"""
    case _ => "// XXX - Unknown field serialised: %s"
  }) + "\n"

  def generateDeserialisationFunction(packet: Packet): String = {
    val template = stdir.template("deserialise")

    // XXX - this is a dirty hack
    val nesteds = packet.fields.filter(_.isInstanceOf[NestedField]).zipWithIndex.toMap

    template.setAttribute("content",
      packet.fields.map { f =>
        "val " + f.info.name.get + " = " + (f match {
          case f: NestedField => deserialiseNested(packet.name, nesteds(f), f)
          case f: Field => deserialiseField(f)
        }) + "\n"
      })
    template.setAttribute("class", packet.name)
    template.setAttribute("params", packet.fields.map(_.info.name.get).mkString(", "))

    template.toString
  }
  def deserialiseFieldType(fieldType: FieldType): String = (fieldType match {
    case Int8 => "buf.get()"
    case Int16 => "buf.getShort()"
    case Int32 => "buf.getInt()"
    case Int64 => "buf.getLong()"
    case Float => "buf.getFloat()"
    case Vec2 => "Vector2(buf.getFloat(), buf.getFloat())"
    case Vec3 => "Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat())"
    case Vec4 => "Vector4(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat())"
    case Uuid16 => "// XXX - Implement Uuid16 deserialisation"
    case Uuid28 => "// XXX - Implement Uuid28 deserialisation"
    case AgentId => "buf.getInt() // XXX - Implement AgentId"
    case Utf16 => """buf.put(%s.getBytes("UTF-16LE"))"""
    case _ => "// XXX - Unknown field deserialised: %s"

  })

  def deserialiseNested(clazz: String, num: Int, field: NestedField): String = {
    val tmpl = "%s.NestedPacket%s(%s)"

    val cmd = tmpl.format(clazz, num, field.members.map(deserialiseField).mkString(", "))

    if (field.arrayInfo == None)
      cmd
    else {
      val ai = field.arrayInfo.get
      val pre = deserialiseFieldType(ai.prefixType)
      if (ai.fixedLength)
        "List(%s)".format(Iterator.fill(ai.length)(cmd).mkString(", "))
      else """{
                    val tmp = %s
                    Iterator.range(0, tmp).toList.map(_ => %s)
                }""".format(pre, cmd)
    }
  }

  def deserialiseField(field: Field): String = {
    if (field.arrayInfo == None)
      (deserialiseFieldType(field.fieldType)).format(field.info.name.get)
    else {
      val ai = field.arrayInfo.get
      val cmd = deserialiseFieldType(field.fieldType)

      val pre = deserialiseFieldType(ai.prefixType)

      if (field.fieldType == Utf16) {
        // XXX - this should be a StringTemplate!
        // XXX - while currently not the case, Utf16 could also be of fixed length
        """{
                    val tmp = %s
                    val arr = new Array[Byte](tmp * 2)
                    buf.get(arr)
                    new String(arr, "UTF-16LE")
                }
            """.format(pre)
      } else {

        if (ai.fixedLength)
          "List(%s)".format(Iterator.fill(ai.length)(cmd).mkString(", "))
        else
          """{
                    val tmp = %s
                    Iterator.range(0, tmp).toList.map(_ => %s)
                }""".format(pre, cmd)
      }
    }
  }

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
