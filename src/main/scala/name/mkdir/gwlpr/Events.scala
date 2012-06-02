package name.mkdir.gwlpr.events

import name.mkdir.gwlpr.Session
import name.mkdir.gwlpr.packets.Packet


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

