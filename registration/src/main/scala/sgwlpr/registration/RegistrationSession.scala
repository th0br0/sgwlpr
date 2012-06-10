package sgwlpr.registration

import akka.actor.IO.SocketHandle

import sgwlpr.Session

case class RegistrationSession(socket: SocketHandle) extends Session {
}
