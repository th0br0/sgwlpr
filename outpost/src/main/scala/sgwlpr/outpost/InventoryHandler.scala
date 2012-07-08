package sgwlpr.outpost

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.DistrictInfo

import g2c._
import c2g._

import sgwlpr.db._

class InventoryHandler(mapId: Int) extends Handler {

  def handleItemsRequest(session: OutpostSession, packet: InstanceLoadItemsRequestPacket) = { 
    log.debug("Items request received")
    val streamId = 0x42

    val packets : List[Packet] = List(
      new ItemStreamCreatePacket(streamId = streamId),
      new UpdateActiveWeaponsetPacket(streamId = streamId, activeSet = 0),
      new CreateInventoryPagePacket(streamId = streamId,
        inventoryType = InventoryType.Equipped.id.toByte, // XXX - write some implicit Val -> byte !
        storageType = StorageType.Equipped.id.toByte,
        pageId = StorageType.Equipped.id,
        slotCount = 9.toByte,
        associatedItemId = 0),
      new UpdateGoldOnCharacterPacket(streamId = streamId, amount = 1337),
  
      new ItemStreamTerminatorPacket(mapId = mapId)

    )

    session.write(packets)
  }

  addMessageHandler(manifest[InstanceLoadItemsRequestPacketEvent], handleItemsRequest)
}
