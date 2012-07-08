package sgwlpr.db

import sgwlpr.types._

import com.mongodb.casbah.Imports
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat._


// TODO: make profession & campaign a proper enumeration 
case class Inventory(@Key("_id") id: ObjectId = new ObjectId,
                     parentId: ObjectId,
                     pages: List[InventoryPage] = List(InventoryPage(0, 9, InventoryType.Equipped, StorageType.Equipped)),
                     gold: Int = 0) {
}

// Create ItemId type?
case class InventoryPage(id: Int, slots: Int, inventoryType: InventoryType.Value, storageType: StorageType.Value, associatedItemId: Int = 0)

object Inventory {

  implicit def wc = AccountDAO.defaultWriteConcern

  def create(obj: Inventory) = InventoryDAO.save(obj)
  def update(obj: Inventory) = InventoryDAO.update(
    q = MongoDBObject("_id" -> obj.id),
    t = obj,
    upsert = false,
    multi = false,
    wc = new WriteConcern())

  def findByParent(obj: Character) = InventoryDAO.findOne(MongoDBObject("_id" -> obj.id))
}
