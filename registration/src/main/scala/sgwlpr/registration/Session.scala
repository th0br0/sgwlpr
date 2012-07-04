package sgwlpr.registration

import akka.actor.IO.SocketHandle

import sgwlpr.Session

import sgwlpr.db.Character

case class RegistrationSession(socket: SocketHandle) extends Session {
  var character : Character = new Character()

}
