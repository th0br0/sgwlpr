package sgwlpr.outpost

import akka.actor.IO.SocketHandle
import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import sgwlpr._
import packets._
import events._

import SessionState.SessionState
import login.LoginSession

class Server(val listenAddress: String, val port: Int, mapId: Int, districtInfo: DistrictInfo) extends GameServerTrait[OutpostSession] {
  def initSession(socket: SocketHandle) = OutpostSession(socket)
  
  val sessions : HashMap[UUID, OutpostSession] = HashMap.empty

  def deserialiserForState(state: SessionState) : Deserialiser = state match {
    case SessionState.New => unenc.Deserialise
    case SessionState.Accepted => c2g.Deserialise
  }

  def migrateSession(oldSession: Session, newSession: OutpostSession) = oldSession match {
    case session: LoginSession => {
      newSession.securityKeys = session.securityKeys
      newSession.seed = session.seed
  
      newSession.account = session.account
      
      // Just assume that this only returns one character, eh? :D
      newSession.character = db.Character.findByParent(session.account.get).find{ c => c.name == session.characterName }
      newSession.inventory = db.Inventory.findByParent(newSession.character.get)

      true
    }
    case _ => log.debug("Do not know how to migrate session of type: " + oldSession.getClass); false
  }

  override def preStart = {
    import akka.actor.Props

    super.preStart

    context.actorOf(Props(new ClientAcceptedHandler(districtInfo)), name="clientAccepted")
    context.actorOf(Props(new InventoryHandler(mapId)), name="inventory")
  }
}
