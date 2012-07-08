package sgwlpr.db

object InventoryType extends Enumeration {
  val Bag = new Val(1)
  val Equipped = new Val(2)
  val Uncollected = new Val(3)
  val Storage = new Val(4)
  val MaterialStorage = new Val(5)

  implicit def value2byte(v: Value) : Byte = v.id.toByte
}

object StorageType extends Enumeration {
  val Backpack = new Val(0)
  val BeltPouch = new Val(1)
  val Bag1 = new Val(2)
  val Bag2 = new Val(3)
  val EquipmentPack = new Val(4)
  val StorageMaterials = new Val(5)
  val UnclaimedItems = new Val(6)
  val Storage1 = new Val(7)
  val Storage2 = new Val(8)
  val Storage3 = new Val(9)
  val Storage4 = new Val(10)
  val Storage5 = new Val(11)
  val Storage6 = new Val(12)
  val Storage7 = new Val(13)
  val Storage8 = new Val(14)
  val StorageAnniversary = new Val(15)
  val Equipped = new Val(16)
  
  implicit def value2byte(v: Value) : Byte = v.id.toByte
}

object ItemType extends Enumeration{
  val Salvage = new Val(0)
  val LeadHand = new Val(1)
  val Axe = new Val(2)
  val Bag = new Val(3)
  val Feet = new Val(4)
  val Bow = new Val(5)
  val Bundle = new Val(6)
  val Chest = new Val(7)
  val Rune = new Val(8)
  val Consumable = new Val(9)
  val Dye = new Val(10)
  val Material = new Val(11)
  val Focus = new Val(12)
  val Arms = new Val(13)
  val Sigil = new Val(14)
  val Hammer = new Val(15)
  val Head = new Val(16)
  val SalvageItem = new Val(17)
  val Key = new Val(18)
  val Legs = new Val(19)
  val Coins = new Val(20)
  val QuestItem = new Val(21)
  val Wand = new Val(22)
  val Shield = new Val(24)
  val Staff = new Val(26)
  val Sword = new Val(27)
  val Kit = new Val(29)
  val Trophy = new Val(30)
  val Scroll = new Val(31)
  val Daggers = new Val(32)
  val Present = new Val(33)
  val Minipet = new Val(34)
  val Scythe = new Val(35)
  val Spear = new Val(36)
  val Handbook = new Val(43)
  val CostumeBody = new Val(44)
  val CostumeHead = new Val(45)

  implicit def value2byte(v: Value) : Byte = v.id.toByte
}

object ItemFlag extends Enumeration{
  val RarityUnique = new Val(0x10) // green item name
  val Undroppable = new Val(0x100)
  val RarityRare = new Val(0x20000) // golden item name
  val RarityUncommon = new Val(0x40000) // purple item name
  val PvP = new Val(0x1000000)
}
