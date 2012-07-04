package sgwlpr.login

import java.util.Random

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.db.Account

import c2l._
import l2c._

class AccountHandler extends Handler {
  def handlePlay(session: LoginSession, packet: PlayPacket) = {
    session.account = Account.findByEmail(session.account.get.email)
    session.heartbeat = packet.heartbeat

    // XXX - supposedly, check that the provided characterName truly is in use ... otherwise disconnect the client


    // XXX - should this really go in here? why not emit some event that causes these packets to be sent...
    session.write(List(
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

  def handleDeleteCharacter(session: LoginSession, packet: DeleteCharacterPacket) = {
    session.account = session.account.map { a => a.copy(characters = a.characters.filterNot(_.name.get == packet.characterName)) }
    Account.save(session.account.get)

    log.debug(session.account.get.characters.toString)


    // XXX - check whether deletion actually works / character exists

    session.heartbeat += 1
    session.write(new StreamTerminatorPacket(session.heartbeat, ErrorCode.None))
  }

  def handlePasswordChange(session: LoginSession, packet: c2l.Packet25): Unit = {
    // XXX - Unknown
  }

  def handleLogout(session: LoginSession, packet: LogoutPacket) = {
  }

  addMessageHandler(manifest[c2l.Packet25Event], handlePasswordChange)
  addMessageHandler(manifest[PlayPacketEvent], handlePlay)
  addMessageHandler(manifest[DeleteCharacterPacketEvent], handleDeleteCharacter)
}
