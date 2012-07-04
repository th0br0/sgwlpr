package sgwlpr.login

import akka.actor.IO.SocketHandle

import sgwlpr.Session
import sgwlpr.db.Account

case class LoginSession(socket: SocketHandle) extends Session {
  var heartbeat = 0
  var account: Option[Account] = None
}
