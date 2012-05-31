package name.mkdir.gwlpr.login

import akka.actor.IO.SocketHandle

import name.mkdir.gwlpr.Session

case class LoginSession(socket: SocketHandle) extends Session {
    var heartbeat = 0
}
