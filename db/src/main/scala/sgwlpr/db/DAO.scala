package sgwlpr.db

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

object AccountDAO extends SalatDAO[Account, ObjectId]( 
  collection = MongoConnection()("sgwlpr")("accounts")
)
