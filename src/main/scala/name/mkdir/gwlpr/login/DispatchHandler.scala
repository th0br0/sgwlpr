package name.mkdir.gwlpr.login

import java.net.InetAddress

import akka.pattern.ask
import akka.util.duration._ 

import name.mkdir.gwlpr.{LookupServer, ServerInfo, ServerNotFound}
import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.events._

import c2l._
import l2c._

class DispatchHandler extends Handler {
  val manager = context.actorFor("/user/manager")

  def buildServerInfo(ip: String, port: Int) : List[Byte] = {
    val addr = InetAddress.getByName(ip).getAddress.toList
    List(2, 0).map(_.toByte) ::: addr ::: List(port >> 8, port & 0xFF).map(_.toByte)
  }

  def handleDispatch(session: LoginSession, request: DispatchRequestPacket) = {
    session.heartbeat = request.heartbeat
    log.debug("mapId: " + request.mapId)

    (manager.ask(LookupServer(request.mapId))(5 seconds)) onSuccess {
        case ServerInfo(server, ip, port) => 
          {
            session.write(new DispatchPacket(
              session.heartbeat,
              session.securityKeys(0),
              request.mapId,
              buildServerInfo(ip, port),
              session.securityKeys(1)
            ))
          }
        case ServerNotFound => session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.NetworkError))
      } onFailure {
        // XXX - failure is only when we time out, right?
        case _ => session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.NetworkError))
      }

  }

  addMessageHandler(manifest[DispatchRequestPacketEvent], handleDispatch)
}
