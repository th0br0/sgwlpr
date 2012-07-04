package sgwlpr.registration

import akka.actor.IO.SocketHandle

import sgwlpr.Session

import sgwlpr.db.Character
import sgwlpr.db.Account

case class RegistrationSession(socket: SocketHandle) extends Session {
  var character : Character = new Character()

  var account: Option[Account] = None
}
