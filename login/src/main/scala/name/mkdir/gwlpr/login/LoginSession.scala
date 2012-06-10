package sgwlpr.login

import akka.actor.IO.SocketHandle

import sgwlpr.Session

case class LoginSession(socket: SocketHandle) extends Session {
  var heartbeat = 0
}
