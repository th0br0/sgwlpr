package name.mkdir.gwlpr

import login.LoginServer
import akka.actor.{ActorSystem, Props}

object Main extends App {
    val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8112
    val system = ActorSystem()

    system.actorOf(Props(new LoginServer(port)), name="login")

    while(readLine != "exit") {}
    system.shutdown

}
