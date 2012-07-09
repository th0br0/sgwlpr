package sgwlpr.world

abstract class Behaviour(implicit val owner: Entity) {
  def onUpdate(tick: Tick)
  def onMessage : PartialFunction[Event, Unit] 
}
