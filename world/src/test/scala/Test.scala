package sgwlpr.world

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import org.specs2.mutable._

class SimpleBehaviour(implicit owner: GameObject) extends Behaviour {
  def onMessage = {
    case m: AttributeValueChanged[_] => {
      println(m.key)
      println(owner.getAttribute(m.key).get.value)
    }
    case _ => 
  }
  def onUpdate(tick: Tick) = {
    println("Woohoo! . delta: " + tick.delta)

    val attr = owner.getAttribute[IntAttribute].get
    println("mmh: " + attr.value)
    attr.value = 42
  }
}

class IntAttribute(implicit owner: GameObject) extends Attribute[Int](1337) {
  def emitValueChanged() = owner.self ! AttributeValueChanged[IntAttribute]
}

object Main extends Specification {
  val system = ActorSystem()
  val go = system.actorOf(Props(new GameObject("test") {
      addBehaviour(new SimpleBehaviour)
      addAttribute(new IntAttribute)
    }), name="gotest")

  go ! Tick(System.currentTimeMillis)

  Thread.sleep(5)
  system.shutdown
}
