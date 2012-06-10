package sgwlpr.registration

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.types._

import g2c._
import c2g._


class CharacterHandler extends Handler {
  val agentId : AgentId = 50 

  def createCharacter(session: RegistrationSession, packet: CreateNewCharacterPacket) = {
    session.write(List(
      new UpdateAttributePointsPacket(agentId, 0, 0),
      new UpdateIntValuePacket(64, agentId, 0),
      new CreateNewCharacterAcknowledgePacket
    ))

    log.debug("weird shit!")
  }

  def setCharacterProfession(session: RegistrationSession, packet: SetCharacterProfessionPacket) = {
    // Set initial map based upon campaign!
    val startMap = packet.campaign match {
      case 0 => /* pvp */ 248
      case 1 => /* prophecies */ 148
      case 2 => /* factions */ 505
      case 3 => /* nightfall */ 449
    }

    // XXX - this should be an implicit or the like
    val isPvp : Byte = {if (packet.campaign == 1) 1 else 0 }.toByte

    // XXX - this is an awkward packet name
    session.write(new UpdatePrivateProfessionsPacket(
      agentId, packet.profession, 0, isPvp
    ))

  }

  def validateNewCharacter(session: RegistrationSession, packet: ValidateNewCharacterPacket) = {

  }

  addMessageHandler(manifest[CreateNewCharacterPacketEvent], createCharacter)
  addMessageHandler(manifest[SetCharacterProfessionPacketEvent], setCharacterProfession)
  addMessageHandler(manifest[ValidateNewCharacterPacketEvent], validateNewCharacter)
}
