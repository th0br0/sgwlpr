package sgwlpr.db

import sgwlpr.types._

import com.mongodb.casbah.Imports
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat._


// TODO: make profession & campaign a proper enumeration 
case class Character(@Key("_id") id: ObjectId = new ObjectId,
                     parentId: ObjectId,
                     name: Option[String] = None, 
                     level: Int = 1,
                     isPvp: Boolean = false,
                     mapId: Option[Int] = None,
                     appearance: Option[CharacterAppearance] = None) {


  def toBytes : List[Byte] = {
    if(mapId == None || appearance == None) throw new RuntimeException("Tried to serialise Character without setting a mapId & appearance first!")
    
    val app = appearance.get
    
    6 ::: mapId.get ::: List[Byte](0x33, 0x36, 0x31, 0x30) ::: List[Byte](
      app.skinColor << 5 | app.height << 1 | app.sex,
      app.face << 7 | app.hairColor << 2 | app.skinColor >> 3,
      app.profession << 4 | app.face >> 1,
      app.campaign << 6) ::: Iterator.fill(16)(0.toByte).toList ::: List[Byte](
      level << 4 | app.campaign,
      // this is actually 
      // 128
      // (showHelm ? 64 : 0)
      // professionSecondary << 2
      // isPvp ? 1 : 0
      // (level > 15) ? 1:0
      (128 | 64 | (2 << 2) | 0 | 0), 0xDD, 0xDD, 0, 0xDD, 0xDD, 0xDD, 0xDD)
  } 
              
}

case class CharacterAppearance(
  // XXX - these supposedly are Bytes, but salat doesn't like Bytes :(
  profession: Int,
  campaign: Int,
  sex: Int,
  height: Int,
  skinColor: Int,
  hairColor: Int,
  hairstyle: Int,
  face: Int
) {
//  private def this(a: Int, b: Int, c: Int, d: Int, e: Int, g: Int, h: Int, i: Int) = this(a.toByte, b.toByte, c.toByte, d.toByte, e.toByte, f.toByte, g.toByte, h.toByte, i.toByte)
}

object CharacterAppearance {
  def apply(data: List[Byte]) : CharacterAppearance = CharacterAppearance(
    profession = ((data(2) >> 4) & 0xFF).toByte,
    campaign = ((data(3) >> 6) & 3).toByte,
    sex = (data(0) & 1).toByte,
    height = ((data(0) >> 1) & 0xFF).toByte,
    skinColor = (((data(0) >> 5) | (data(1) << 3)) & 0xFF).toByte,
    hairColor = ((data(1) >> 2) & 0xFF).toByte,
    hairstyle = (data(3) & 0x1F).toByte,
    face = (((data(1) >> 7) | (data(2) << 1)) & 0x1F).toByte
  )
}

object Character {
  implicit def wc = AccountDAO.defaultWriteConcern

  def create(acc: Character) = AccountDAO.characters.save(acc)
  def delete(acc: Character) = AccountDAO.characters.remove(MongoDBObject("_id" -> acc.id))
  def update(acc: Character) = AccountDAO.characters.update(
    q = MongoDBObject("_id" -> acc.id),
    t = acc,
    upsert = false,
    multi = false,
    wc = new WriteConcern())

  def findByParent(acc: Account) = AccountDAO.characters.findByParentId(acc.id).toList
  def deleteWithName(acc: Account, name: String) = AccountDAO.characters.remove(MongoDBObject("parentId" -> acc.id, "name" -> name))
}
