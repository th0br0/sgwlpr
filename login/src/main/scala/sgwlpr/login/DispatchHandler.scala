package sgwlpr.login

import java.net.InetAddress

import akka.pattern.ask
import akka.util.duration._ 

import sgwlpr.{ServerInfo, LookupServer, SessionTransit}
import sgwlpr.packets._
import sgwlpr.events._

import c2l._
import l2c._

class DispatchHandler extends Handler {
  val manager = context.actorFor("/user/manager")

  def buildServerInfo(ip: String, port: Int) : List[Byte] = {
    val addr = InetAddress.getByName(ip).getAddress.toList

    val ret = List(2, 0).map(_.toByte) ::: List(port >> 8, port & 0xFF).map(_.toByte) ::: addr

    ret ::: (Iterator.fill(24 - ret.length)(0.toByte)).toList
  }

  def handleDispatch(session: LoginSession, request: DispatchRequestPacket) = {
    session.heartbeat = request.heartbeat
    log.debug("mapId: " + request.mapId)

    (manager.ask(LookupServer(request.mapId))(5 seconds)) onSuccess {
        case ServerInfo(server, ip, port) => 
          {
            server ! SessionTransit(session)
            session.write(new DispatchPacket(
              session.heartbeat,
              session.securityKeys(0),
              request.mapId,
              buildServerInfo(ip, port),
              session.securityKeys(1)
            ))
          }
        //case ServerNotFound => session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.NetworkError))
      } onFailure {
        // XXX - failure is only when we time out, right?
        case _ => session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.NetworkError))
      }

  }

  addMessageHandler(manifest[DispatchRequestPacketEvent], handleDispatch)
}
