package sgwlpr

import login.LoginServer
import registration.RegistrationServer
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

  system.actorOf(Props(new ServerManager), name="manager")

  system.actorOf(Props(new LoginServer(port)), name="login")
  system.actorOf(Props(new RegistrationServer(port + 1)), name="registration")

  while(readLine != "exit") {}
  system.shutdown

}
