package sgwlpr.registration

import akka.actor.IO.SocketHandle
import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import sgwlpr._
import packets._
import events._

import SessionState.SessionState
import login.LoginSession

class Server(val listenAddress: String, val port: Int) extends GameServerTrait[RegistrationSession] {
  def initSession(socket: SocketHandle) = RegistrationSession(socket)
  
  val sessions : HashMap[UUID, RegistrationSession] = HashMap.empty

  def deserialiserForState(state: SessionState) : Deserialiser = state match {
    case SessionState.New => unenc.Deserialise
    case SessionState.Accepted => c2g.Deserialise
  }

  def migrateSession(oldSession: Session, newSession: RegistrationSession) = oldSession match {
    case session: LoginSession => {
      newSession.securityKeys = session.securityKeys
      newSession.seed = session.seed
  
      newSession.account = session.account
      
      true
    }
    case _ => log.debug("Do not know how to migrate session of type: " + oldSession.getClass); false
  }

  override def preStart = {
    import akka.actor.Props

    super.preStart
    context.actorOf(Props(new ClientAcceptedHandler), name="clientAccepted")
    context.actorOf(Props(new CharacterHandler), name="character")
  }
}
