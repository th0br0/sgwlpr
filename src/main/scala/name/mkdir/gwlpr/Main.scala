package name.mkdir.gwlpr

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress

import login.LoginServer

object Main extends App {
  val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8112
  val system = ActorSystem()
  val server = system.actorOf(Props(new LoginServer(port)), name="login")
}
