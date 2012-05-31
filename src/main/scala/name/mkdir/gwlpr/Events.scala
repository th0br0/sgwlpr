package name.mkdir.gwlpr.events

import name.mkdir.gwlpr.Session
import name.mkdir.gwlpr.packets.Packet
import name.mkdir.gwlpr.Logging

import akka.actor.Actor
import akka.actor.ActorLogging

trait Event
trait ClientEvent extends Event {
    def session: Session
}

case class ClientConnected(session: Session) extends ClientEvent
case class ClientDisconnected(session: Session) extends ClientEvent

trait ClientMessageEvent extends ClientEvent {
    def packet : Packet
    def session : Session
}

//-----------------
trait Handler extends Actor with ActorLogging {
    def subscribeTo(c: Class[_]) = context.system.eventStream.subscribe(self, c)
}
