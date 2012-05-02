package name.mkdir.gwlpr

import com.eaio.uuid.UUID

sealed abstract trait Event {}

case class NewClientEvent(session: Session) extends Event
case class MessageEvent(uuid: UUID, packets: List[Packet]) extends Event
