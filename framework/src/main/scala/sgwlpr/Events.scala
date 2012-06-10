package sgwlpr.events

import sgwlpr.Session
import sgwlpr.packets.Packet


trait Event

case class SubscribeToEvent(clazz: Class[_]) 

trait ClientEvent extends Event {
  def session: Session
}

case class ClientAccepted(session: Session) extends ClientEvent
case class ClientConnected(session: Session) extends ClientEvent
case class ClientDisconnected(session: Session) extends ClientEvent

trait ClientMessageEvent extends ClientEvent {
  def packet : Packet
  def session : Session
}

