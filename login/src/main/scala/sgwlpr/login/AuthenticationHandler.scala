package sgwlpr.login

import java.util.Random

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.db.Account

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

    val res = Account.findWithEmail(email)

    if(res == None) {
      // XXX - check for valid character name here!
      log.debug("Created account!")
      session.account = Some(Account(email = email, password = password))
      Account.create(session.account.get)
    } else if( res.get.password == password ) {
      session.account = res
      return true
    }

    return false
  }

  def handleLogin(session: LoginSession, packet: LoginPacket) : Unit = {
    session.heartbeat = packet.heartbeat
    log.debug("Client login: " + packet.email)
    log.debug("client pw: " + password(packet))
    log.debug("Character name: " + packet.charName)

    if(!performLogin(session, packet.email, password(packet), packet.charName)) {
      // XXX - publish a clientdisconnected event
      session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.UnknownUser))
      return
    }

    // Login successful -- put this in a separate method?
    session.write(List(
      new GuiSettingsPacket(session.heartbeat, List[Byte](0)),
      new FriendsListEndPacket(session.heartbeat, 1),
      // XXX - analyse the values here...
      new AccountInfoPacket(
        session.heartbeat, 2, 4, 
        List(0x3F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte),
        List(0x80, 0x3F, 0x02, 0x00, 0x03, 0x00, 0x08, 0x00).map(_.toByte),
        List(0x37, 0x4B, 0x09, 0xBB, 0xC2, 0xF6, 0x74, 0x43, 0xAA, 0xAB, 0x35, 0x4D, 0xEE, 0xB7, 0xAF, 0x08).map(_.toByte),
        List(0x55, 0xB6, 0x77, 0x59, 0x0C, 0x0C, 0x15, 0x46, 0xAD, 0xAA, 0x33, 0x43, 0x4A, 0x91, 0x23, 0x6A).map(_.toByte),
        8, List(0x01, 0x00, 0x06, 0x00, 0x57, 0x00, 0x01, 0x00).map(_.toByte), 23, 0),
      new StreamTerminatorPacket(session.heartbeat, ErrorCode.None)
    ))

}

def handlePasswordChange(session: LoginSession, packet: c2l.Packet25): Unit = {
  // XXX - Unknown
}

def handleLogout(session: LoginSession, packet: LogoutPacket) = {
}

addMessageHandler(manifest[LoginPacketEvent], handleLogin)
addMessageHandler(manifest[LogoutPacketEvent], handleLogout)
addMessageHandler(manifest[c2l.Packet25Event], handlePasswordChange)
}
