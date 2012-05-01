package name.mkdir.gwlpr

abstract sealed trait Event {}

case class MessageEvent(session: Session, packets: List[Packet]) extends Event
