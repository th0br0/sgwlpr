package sgwlpr.world

import scala.collection.mutable.{HashMap, ListBuffer}

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.event.EventStream

import sgwlpr.Session
import sgwlpr.packets.Packet
import sgwlpr.db.{Character, Inventory}

class World(val mapId: Int) extends Actor with ActorLogging {

  // XXX - ahahahahahah ;)
  object Counter {
    var count = 0
    def get = {
      count = count + 1
      count - 1
    }
  }

  // XXX - use a hashset instead of a listbuffer?
  val eventStream = new EventStream
  val entities = HashMap.empty[Int, ActorRef]
  val sessions = HashMap.empty[ActorRef, Session]
  
  def receive : Receive = {
    case SubscribeTo(c) => eventStream.subscribe(sender, c)
    case UnsubscribeFrom(c) => eventStream.unsubscribe(sender, c)
    case out: OutboundMessage => sessions(sender) write (out.toPackets)
    case RegisterPlayer(session, c, i) => {
      val ref = context.actorOf(Props(new Player(c, i)), name=("player" + session.socket.hashCode))
      val id = Counter.get

      eventStream.subscribe(ref, classOf[Tick])

      sessions += (ref -> session)
      entities += (id -> ref)    
      sender ! Registered(id, ref)
    }
    case 'Tick => eventStream.publish(Tick(System.currentTimeMillis))
  }

  override def preStart = {
    import akka.util.duration._
    super.preStart

    val updatesPerSecond = 30
    context.system.scheduler.schedule(0 milliseconds, (1000 / 30).toInt milliseconds, self, 'Tick)
  }
}
