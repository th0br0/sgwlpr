package name.mkdir.gwlpr.registration

import akka.actor.IO.SocketHandle

import name.mkdir.gwlpr.Session

case class RegistrationSession(socket: SocketHandle) extends Session {
}
