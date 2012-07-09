package sgwlpr.world


import akka.actor.ActorRef
import sgwlpr.Session
import sgwlpr.db.{Character, Inventory}
import sgwlpr.packets.Packet

trait Event 

case class AttributeValueChanged[T <: BaseAttribute[_]](implicit val key: Manifest[T]) extends Event

case class Tick(at: Long) {
  def delta = System.currentTimeMillis - at
}

trait OutboundMessage {
  def toPackets : List[Packet]
}

// Outside to World
// XXX - maybe we should move Account, Character (& Inventory?) into the base session trait?
case class RegisterPlayer(session: Session, character: Character, inventory: Inventory)
case class RegisterEntity(ent: Entity)

// World to Outside
case class Registered(id: Int, ref: ActorRef)

// Outside to Entity
case class Request(sym: Symbol)

// Entity to World
case class SubscribeTo(classifier: Class[_])
case class UnsubscribeFrom(classifier: Class[_])
// XXX - should there be some AttachInfo that is sent back from an entity upon attachment?

// World to Entity
case class AttachedToWorld(world: ActorRef)

