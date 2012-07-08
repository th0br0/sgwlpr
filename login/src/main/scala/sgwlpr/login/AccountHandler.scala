package sgwlpr.login

import java.util.Random

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.db._

import c2l._
import l2c._

class AccountHandler extends Handler {
  def handlePlay(session: LoginSession, packet: PlayPacket) = {
    session.account = Account.findByEmail(session.account.get.email)
    session.heartbeat = packet.heartbeat

    // XXX - confirm validity of characterName, otherwise we've got some badly behaving client ;) 

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

  def handleCharacterDelete(session: LoginSession, packet: CharacterDeletePacket) = {
    Character.deleteWithName(session.account.get, packet.characterName)

    // XXX - implement deletion confirmation

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
  addMessageHandler(manifest[CharacterDeletePacketEvent], handleCharacterDelete)
}
