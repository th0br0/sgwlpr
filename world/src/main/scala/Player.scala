package sgwlpr.world

import scala.collection.mutable.HashMap

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging

import sgwlpr.db.Character
import sgwlpr.db.Inventory
import sgwlpr.types._

case class CharacterDataMessage(character: Character, inventory: Inventory) extends OutboundMessage {
  val streamId : Short = 0x42

  import sgwlpr.packets.g2c._

  private def inventoryPagePackets = inventory.pages map { page => new CreateInventoryPagePacket(
    streamId,
    page.inventoryType,
    page.storageType,
    page.id,
    page.slots,
    page.associatedItemId
  )}

  def toPackets = 
    List(
      new ItemStreamCreatePacket(streamId = streamId),
      new UpdateActiveWeaponsetPacket(streamId = streamId, activeSet = 0)
    ) ::: inventoryPagePackets ::: List(
      new UpdateGoldOnCharacterPacket(streamId = streamId, amount = inventory.gold),
      new ItemStreamTerminatorPacket(mapId = 1) // XXX - it seems that this doesn't need to be the mapId ?
    )
}

// XXX - Inventory and Character should be attributes! =)
class Player(val character: Character, val inventory: Inventory) extends Entity(character.name.get) {

    override def receive = {
      case Request('SpawnPoint) => log.debug("send the client that spawnpoint data")
      case Request('CharacterData) => log.debug("send the client that character data"); publish(CharacterDataMessage(character, inventory))
      case e => super.receive(e)
    }

}
