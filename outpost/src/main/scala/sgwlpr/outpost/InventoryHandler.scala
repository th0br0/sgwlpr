package sgwlpr.outpost

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.DistrictInfo

import g2c._
import c2g._

import sgwlpr.db._
import sgwlpr.types._

class InventoryHandler(mapId: Int) extends Handler {

  private def inventoryPagePackets(streamId: Int, pages: List[InventoryPage]) : List[Packet] = pages.map { page =>
    new CreateInventoryPagePacket(
      streamId,
      page.inventoryType,
      page.storageType,
      page.id,
      page.slots,
      page.associatedItemId)
  }

  def handleItemsRequest(session: OutpostSession, packet: InstanceLoadItemsRequestPacket) = { 
    log.debug("Items request received")
    val streamId = 0x42

    session.write(new ItemStreamCreatePacket(streamId = streamId))
    session.inventory.map { inventory => 
      session.write(new UpdateActiveWeaponsetPacket(streamId = streamId, activeSet = 0))
      session.write(inventoryPagePackets(streamId, inventory.pages))
      session.write(new UpdateGoldOnCharacterPacket(streamId = streamId, amount = inventory.gold))
    }

    session.write(new ItemStreamTerminatorPacket(mapId = mapId))
  }

  addMessageHandler(manifest[InstanceLoadItemsRequestPacketEvent], handleItemsRequest)
}
