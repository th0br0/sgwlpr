package sgwlpr.db

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.WriteConcern


case class Account(@Key("_id") id : ObjectId = new ObjectId, email: String, password: String)

object Account {
  implicit def wc = AccountDAO.defaultWriteConcern

  def create(acc: Account) = AccountDAO.save(acc)
  def delete(acc: Account) = AccountDAO.remove(MongoDBObject("_id" -> acc.id))
  def update(acc: Account) = AccountDAO.update(
    q = MongoDBObject("_id" -> acc.id),
    t = acc,
    upsert = false,
    multi = false,
    wc = new WriteConcern())

  def findByEmail(email: String) = AccountDAO.findOne(MongoDBObject("email" -> email))
}
