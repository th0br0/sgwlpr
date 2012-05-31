package name.mkdir.gwlpr.events

import akka.actor.Actor
import akka.actor.ActorLogging
import name.mkdir.gwlpr.Session
import name.mkdir.gwlpr.packets.Packet

trait Handler extends Actor with ActorLogging {
    import scala.collection.mutable.HashMap
    type EventListener = Event => Unit


    private val handlers : HashMap[Class[_], EventListener] = HashMap.empty

    def subscribeTo(c: Class[_]) = context.system.eventStream.subscribe(self, c)

    def receive = {
        case e: Event => {
          val content = handlers.get(e.getClass)

          if(content != None)
            content.get(e)
        }
        case d => 
    }

    def addHandler(clazz: Class[_], fun: EventListener) = {
        handlers += (clazz -> fun)
    }

    def addMessageHandler[T <: ClientMessageEvent, S <: Session, P <: Packet](m: Manifest[T], fun: (S, P) => Unit) = {
        def handler()(e : Event) = {
            val m = e.asInstanceOf[T]
            fun(m.session.asInstanceOf[S], m.packet.asInstanceOf[P])
        }

        log.debug("handling %s now!".format(m.erasure))
        addHandler(m.erasure, handler())
        subscribeTo(m.erasure)
    }
}