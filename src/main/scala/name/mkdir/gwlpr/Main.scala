package name.mkdir.gwlpr

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress

import login.LoginServer
import game.RegistrationServer

object Main extends App {
  val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8112
  val system = ActorSystem()
  system.actorOf(Props(new RegistrationServer(port + 1)), name="registration")
  system.actorOf(Props(new LoginServer(port)), name="login")
}
