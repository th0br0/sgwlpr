package name.mkdir.gwlpr.login

import java.util.Random

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.events._

import c2l._
import l2c._

class GenericHandler extends Handler {
    def handleComputerInfo(session: LoginSession, packet: ComputerInfoPacket) = {
        log.debug("Received ComputerInfo(%s, %s)".format(packet.username, packet.hostname))
        
        session.write(new ComputerInfoReplyPacket(heartbeat = session.heartbeat))
    }

    def handleResponseRequest(session: LoginSession, packet: ResponseRequestPacket) = {
        log.debug("Received ResponseRequest")
        session.heartbeat = packet.heartbeat

        session.write(new ResponseRequestReplyPacket(heartbeat = session.heartbeat))
        session.write(new StreamTerminatorPacket(heartbeat = session.heartbeat))
    }


    addMessageHandler(manifest[ComputerInfoPacketEvent], handleComputerInfo)
    addMessageHandler(manifest[ResponseRequestPacketEvent], handleResponseRequest)
}
