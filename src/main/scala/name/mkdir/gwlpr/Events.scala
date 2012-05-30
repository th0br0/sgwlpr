package name.mkdir.gwlpr.events

import name.mkdir.gwlpr.Session
import name.mkdir.gwlpr.packets.Packet
import name.mkdir.gwlpr.Logging

trait Event

trait EventHandler extends Logging {
    def handleEvent : PartialFunction[Event, Unit] = {
        case e: Event => log.debug("Event %s not handled.".format(e))
    }
}

trait ClientEvent[T <: Session] extends Event {
    def session: T
}

case class ClientConnected[T <: Session](session: T) extends ClientEvent[T]
case class ClientDisconnected[T <: Session](session: T) extends ClientEvent[T]
case class ClientMessage[T <: Session](session: T, packet: Packet) extends ClientEvent[T]

