package sgwlpr

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef

import scala.collection.mutable.HashMap

case class LookupServer(mapId: Int)
case class RegisterServer(mapId: Int, serverInfo: ServerInfo)
case object UnregisterServer
case class ServerInfo(actor: ActorRef, ip: String, port: Int)
case object ServerNotFound

class ServerManager extends Actor with ActorLogging {
  // XXX - this only supports one server per map atm ;)
  val servers : HashMap[Int, ServerInfo] = HashMap.empty

  def receive = {
    case RegisterServer(mapId, serverInfo) => servers += (mapId -> serverInfo)
    case UnregisterServer => log.debug("Implement UnregisterServer")

    case LookupServer(mapId) => {
      val server = servers.get(mapId)
      if(server == None)
        sender ! ServerNotFound
      else
        sender ! server.get
    }
    case _ => 
  }


}
