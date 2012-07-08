package sgwlpr.registration

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.types._

import g2c._
import c2g._

import sgwlpr.db._

class CharacterHandler extends Handler {
  val agentId : AgentId = 50 

  def createCharacter(session: RegistrationSession, packet: CharacterCreatePacket) = {
    session.write(List(
      new UpdateAttributePointsPacket(agentId, 0, 0),
      new UpdateIntValuePacket(64, agentId, 0),
      new CharacterCreateAckPacket
    ))

    session.character = Some(new Character(parentId = session.account.get.id))

    log.debug("weird shit!")
  }

  def setCharacterProfession(session: RegistrationSession, packet: CharacterSetProfessionPacket) = {
    // Set initial map based upon campaign!
    val startMap = packet.campaign match {
      case 0 => /* pvp */ 248
      case 1 => /* prophecies */ 148
      case 2 => /* factions */ 505
      case 3 => /* nightfall */ 449
    }

    // XXX - this should be an implicit or the like
    val isPvp : Byte = {if (packet.campaign == 1) 1 else 0 }.toByte

    session.character = session.character.map (_.copy(isPvp = packet.campaign == 1, mapId = Some(startMap)))

    // XXX - this is an awkward packet name
    session.write(new UpdatePrivateProfessionsPacket(
      agentId, packet.profession, 0, isPvp
    ))

  }

  def validateNewCharacter(session: RegistrationSession, packet: CharacterValidatePacket) = {
    session.character = session.character.map(_.copy(name = Some(packet.characterName), appearance = Some(CharacterAppearance(packet.data))))
    Character.create(session.character.get)

    session.character.map { char => 
      session.write(new Packet378(
        hash = Iterator.fill(16)(0.toByte).toList,
        characterName = char.name.get,
        mapId = char.mapId.get,
        characterData = char.toBytes
      ))
    }
  }

  addMessageHandler(manifest[CharacterCreatePacketEvent], createCharacter)
  addMessageHandler(manifest[CharacterSetProfessionPacketEvent], setCharacterProfession)
  addMessageHandler(manifest[CharacterValidatePacketEvent], validateNewCharacter)
}
