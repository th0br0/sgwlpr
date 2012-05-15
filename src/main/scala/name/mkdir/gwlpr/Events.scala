package name.mkdir.gwlpr

import com.eaio.uuid.UUID

sealed abstract trait Event {}

// TODO: maybe we shouldn't send the ip / port in here.

case object TestEvent extends Event
case class ClientTransferRequest(session: Session) extends Event
case class ClientAccepted(ip: String, port: Int) extends Event
case class ClientRejected extends Event
