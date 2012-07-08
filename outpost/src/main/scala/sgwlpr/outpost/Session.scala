package sgwlpr.outpost

import akka.actor.IO.SocketHandle

import sgwlpr.Session

import sgwlpr.db.Character
import sgwlpr.db.Account

case class OutpostSession(socket: SocketHandle) extends Session {
  var character : Option[Character] = None
  
  var account: Option[Account] = None
}
