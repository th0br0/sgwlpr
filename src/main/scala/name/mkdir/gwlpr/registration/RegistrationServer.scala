package name.mkdir.gwlpr.registration

import akka.actor.IO.SocketHandle
import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import name.mkdir.gwlpr._
import packets._
import events._

import SessionState.SessionState
import login.LoginSession
class RegistrationServer(val port: Int) extends ServerTrait[LoginSession] {
  def initSession(socket: SocketHandle) = LoginSession(socket)
  val sessions : HashMap[UUID, LoginSession] = HashMap.empty

  def deserialiserForState(state: SessionState) : Deserialiser = state match {
    case SessionState.New => unenc.Deserialise
    case SessionState.Accepted => c2g.Deserialise
  }

  override def preStart = {
    super.preStart

    context.actorFor("/user/manager") ! RegisterServer(0, ServerInfo(self, "localhost", port))


  }
}
