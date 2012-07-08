package sgwlpr.manager

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.pattern.ask

import scala.collection.mutable.HashMap

import sgwlpr.login
import sgwlpr.registration
import sgwlpr.outpost
import sgwlpr.{DistrictInfo, LookupServer, ServerInfo}

// ------------------------------------------------------------------------------------
object ServerType extends Enumeration {
  val Outpost, Registration = Value
  type ServerType = Value
}
import ServerType.ServerType

case class StartServer(mapId: Int, serverType: ServerType, districtInfo: DistrictInfo) 
// XXX - how about startup failing? :S
// XXX - support remote server shutdown? 
case class RegisterServerProvider(ref: ActorRef)
// ------------------------------------------------------------------------------------

// XXX - allow specification of portRange which would also limit the amounts of servers one provider can spawn? 
class ServerProvider(listenAddress: String, var currentPort: Int) extends Actor with ActorLogging {
  import ServerType._
  // XXX - keep a local reference to all servers?

  // XXX - ok. no excuse here.

  def receive = {
    case StartServer(mapId, serverType, districtInfo) => {
      log.debug("Starting new Server for %d of type %s with districtInfo: %s".format(mapId, serverType.toString, districtInfo.toString))
      
      val port = currentPort
      currentPort = currentPort + 1

      serverType match {
      // XXX - this is just temporary. it should be context.actorOf !?
      case Registration => sender ! ServerInfo(
        context.system.actorOf(Props(new registration.Server(listenAddress, port)), name=("registration")),
        listenAddress,
        port)
      case Outpost => sender ! ServerInfo(
        context.system.actorOf(Props(new outpost.Server(listenAddress, port, mapId, districtInfo)), name=("outpost-%d-%d-%d".format(mapId, districtInfo.district, districtInfo.region))),
        listenAddress,
        port)
    }}
  } 
}


class ServerManager extends Actor with ActorLogging {
  import akka.util.duration._
  import akka.util.Timeout
  import collection.mutable.ListBuffer
  implicit val timeout = Timeout(5 seconds)

  val providers : ListBuffer[ActorRef] = ListBuffer.empty

  // XXX - this only supports one server per map
  val servers : HashMap[Int, ServerInfo] = HashMap.empty

  def receive = {
    case RegisterServerProvider(ref) => providers += ref
    case LookupServer(mapId) => if(servers.contains(mapId)) {sender ! servers(mapId)} else {

      // XXX - a ListBuffer isn't the best data type for this
      val provider = providers.head
      providers -= provider
      providers += provider

      val serverType = mapId match {
        case 0 => ServerType.Registration
        case _ => ServerType.Outpost
      }
      val client = sender  
      (provider ? StartServer(mapId, serverType, DistrictInfo(1, 1, 1.toByte)) onSuccess {
          case s: ServerInfo => {
            servers += (mapId -> s)
            client ! s
          }
        })
    }
    case _ => 
  }

}
