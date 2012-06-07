package name.mkdir.gwlpr.registration

import akka.actor.IO.SocketHandle
import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import name.mkdir.gwlpr._
import packets._
import events._

import SessionState.SessionState
import login.LoginSession

class RegistrationServer(val port: Int) extends GameServerTrait[RegistrationSession] {
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

      true
    }
    case _ => log.debug("Do not know how to migrate session of type: " + oldSession.getClass); false
  }

  override def preStart = {
    import akka.actor.Props

    super.preStart
    context.actorFor("/user/manager") ! RegisterServer(0, ServerInfo(self, "localhost", port))

    context.actorOf(Props(new ClientAcceptedHandler), name="clientAccepted")
  }
}
