package name.mkdir.gwlpr.login

import java.util.Random

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.events._

import c2l._
import l2c._

class AuthenticationHandler extends Handler {
    def password(packet: LoginPacket): String = {
        // XXX - this is only valid for the unencrypted login
        val length =  (packet.unknown0 & 0xFF)
        val pwlist  = (List((packet.unknown0 >> 16) & 0xFF, (packet.unknown0 >> 24)).map(_.toByte) ::: packet.password.map(_.toByte)).take(length)
        new String(pwlist.toArray, "UTF-16LE")


    }

    def performLogin(session: LoginSession, email: String, password: String, charName: String) : Boolean = {
        log.info("TODO: Implement performLogin")
        return false
    }

    def handleLogin(session: LoginSession, packet: LoginPacket) : Unit = {
        session.heartbeat = packet.heartbeat
        log.debug("Client login: " + packet.email)
        log.debug("client pw: " + password(packet))
        log.debug("Character name: " + packet.charName)

        if(!performLogin(session, packet.email, password(packet), packet.charName)) {
            session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.UnknownUser))
            return
        }

        // Login successful
    }

    def handleLogout(session: LoginSession, packet: LogoutPacket) = {
    }
    
    addMessageHandler(manifest[LoginPacketEvent], handleLogin)
    addMessageHandler(manifest[LogoutPacketEvent], handleLogout)
}
