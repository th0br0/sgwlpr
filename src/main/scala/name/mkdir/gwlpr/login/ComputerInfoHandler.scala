package name.mkdir.gwlpr.login

import java.util.Random

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.events._

import c2l.ComputerInfoPacket
import c2l.ComputerInfoPacketEvent
import l2c.ComputerInfoReplyPacket

class ComputerInfoHandler extends Handler {
    def handlePacket(session: LoginSession, packet: ComputerInfoPacket) = {
        log.debug("Received ComputerInfo(%s, %s)".format(packet.username, packet.hostname))
        
        session.write(new ComputerInfoReplyPacket(heartbeat = session.heartbeat))
    }

    addMessageHandler(manifest[ComputerInfoPacketEvent], handlePacket)
}
