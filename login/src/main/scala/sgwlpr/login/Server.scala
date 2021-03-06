package sgwlpr.login

import akka.actor.IO.SocketHandle
import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import sgwlpr._
import packets._
import events._

import SessionState.SessionState

class Server(val listenAddress: String, val port: Int) extends ServerTrait[LoginSession] {
  def initSession(socket: SocketHandle) = LoginSession(socket)
  val sessions : HashMap[UUID, LoginSession] = HashMap.empty

  def deserialiserForState(state: SessionState) : Deserialiser = state match {
    case SessionState.New => unenc.Deserialise
    case SessionState.Accepted => c2l.Deserialise
  }

  override def preStart = {
    import akka.actor.Props

    super.preStart

    // XXX - is there some way to define the name inside the actor as with akka 1.* ?
    context.actorOf(Props(new GenericHandler), name="generic")
    context.actorOf(Props(new AuthenticationHandler), name="authentication")
    context.actorOf(Props(new DispatchHandler), name="dispatch")
    context.actorOf(Props(new AccountHandler), name="account")
  }
}
