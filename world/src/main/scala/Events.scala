package sgwlpr.world

// Do we really need to do this?
import sgwlpr.events.Event

import akka.actor.ActorRef

trait WorldEvent extends Event

case class AttributeValueChanged[T <: BaseAttribute[_]](implicit val key: Manifest[T]) extends WorldEvent

case class Tick(at: Long) {
  def delta = System.currentTimeMillis - at
}

