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
        
        session.write(new ComputerInfoReplyPacket(
            0x71953D3D, // XXX - weird static data
            session.heartbeat,
            0,
            1
        ))
    }

    addMessageHandler(manifest[ComputerInfoPacketEvent], handlePacket)
}
