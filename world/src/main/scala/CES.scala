package sgwlpr.world

import scala.collection.mutable.HashMap

import akka.actor.Actor
import akka.actor.ActorLogging

trait GameEvent

case class AttributeValueChanged[T <: BaseAttribute[_]](implicit val key: Manifest[T]) extends GameEvent
case class Tick(at: Long) {
  def delta = System.currentTimeMillis - at
}
//--------------------------------------------------------------------------------------------

abstract class BaseAttribute[T](protected var _value: T)(implicit val owner: GameObject) {
  def value: T = _value
}

abstract class ConstAttribute[T](initialValue: T)(implicit _owner: GameObject) extends BaseAttribute[T](initialValue)
abstract class Attribute[T](initialValue: T)(implicit _owner: GameObject) extends BaseAttribute[T](initialValue) {
  def value_=(newval: T) = {
    _value = newval
    emitValueChanged()
  }

  protected def emitValueChanged()
}

//--------------------------------------------------------------------------------------------

abstract class Behaviour(implicit val owner: GameObject) {
  def onUpdate(tick: Tick)
  def onMessage : PartialFunction[GameEvent, Unit] 
}

//--------------------------------------------------------------------------------------------

class GameObject(val name: String) extends Actor with ActorLogging{
    implicit val owner = this 

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


    def receive = {
      case evt: GameEvent => behaviours.values.foreach (_.onMessage(evt))
      case tick: Tick => behaviours.values.foreach (_.onUpdate(tick))
    }

}
