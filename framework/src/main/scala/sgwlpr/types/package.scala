package sgwlpr

package object types {
  implicit def int2AgentId(i: Int) : AgentId = AgentId(i)
  implicit def agentId2Int(a: AgentId) : Int = a.id

  implicit def int2Byte(i: Int) : Byte = i.toByte
  implicit def int2ListByte(i: Int) : List[Byte] = List(
    i & 0xFF,
    (i >> 8) & 0xFF
  )
}
