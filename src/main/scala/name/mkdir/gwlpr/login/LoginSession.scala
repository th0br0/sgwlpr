package name.mkdir.gwlpr.login

import akka.actor.IO.SocketHandle

import name.mkdir.gwlpr.Session

case class LoginSession(private val _socket: SocketHandle) extends Session(_socket)
