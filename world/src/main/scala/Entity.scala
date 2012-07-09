package sgwlpr.world

import scala.collection.mutable.HashMap

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging

class Entity(val name: String) extends Actor with ActorLogging{
    // XXX - This might be confusing
    implicit val owner = this 
    val world = context.actorFor("../../world")

    type Attr = BaseAttribute[_]

    class Registry[B] {
      private val map = HashMap.empty[Manifest[_ <: B], B]
      def add[T <: B](value: T)(implicit m: Manifest[T]) = map += (m -> value)
      def get[T <: B](implicit m: Manifest[T]): Option[T] = map get m map (_.asInstanceOf[T])
      def remove[T <: B](implicit m: Manifest[T]) = map remove m
      def values = map.values
    }

    val behaviours = new Registry[Behaviour]
    val attributes = new Registry[Attr]

    def addAttribute[T <: Attr](value: T)(implicit m: Manifest[T]) = attributes.add(value)
    def addBehaviour[T <: Behaviour](value: T)(implicit m: Manifest[T]) = behaviours.add(value)

    def getAttribute[T <: Attr](implicit m: Manifest[T]) = attributes.get
    def getBehaviour[T <: Behaviour](implicit m: Manifest[T]) = behaviours.get

    def removeAttribute[T <: Attr](implicit m: Manifest[T]) = attributes.remove
    def removeBehaviour[T <: Behaviour](implicit m: Manifest[T]) = behaviours.remove

    def publish(v: Any) = world ! v

    def receive = {
      case evt: Event => behaviours.values.foreach (_.onMessage(evt))
      case tick: Tick => behaviours.values.foreach (_.onUpdate(tick))
    }

}
