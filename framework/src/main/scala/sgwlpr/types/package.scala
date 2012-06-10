package sgwlpr

package object types {
  implicit def int2AgentId(i: Int) : AgentId = AgentId(i)
  implicit def agentId2Int(a: AgentId) : Int = a.id
}
