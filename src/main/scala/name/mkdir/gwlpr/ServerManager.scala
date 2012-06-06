package name.mkdir.gwlpr

import akka.actor.Actor
import akka.actor.ActorRef

case class LookupServer(mapId: Int)
case class RegisterServer(mapId: Int, serverInfo: ServerInfo)
case class ServerInfo(actor: ActorRef, ip: String, port: Int)
case object ServerNotFound

