package sgwlpr

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.parseString("""
    akka {
      loglevel = DEBUG
    }
  """)
  
  val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8112
  val system = ActorSystem("default", config = config)

  val listenAddress = "127.0.0.1"
  val serverManager = system.actorOf(Props(new manager.ServerManager), name="manager")
  serverManager ! manager.RegisterServerProvider(
    system.actorOf(Props(new manager.ServerProvider(listenAddress, 9000)), name="provider")
  )
  
  system.actorOf(Props(new login.Server(listenAddress, 7999)), name="login")

  while(readLine != "exit") {}
  system.shutdown

}
