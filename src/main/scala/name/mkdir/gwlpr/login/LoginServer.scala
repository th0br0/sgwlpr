package name.mkdir.gwlpr.login

import akka.actor.IO.SocketHandle

import com.eaio.uuid.UUID

import scala.collection.mutable.HashMap

import java.nio.ByteBuffer

import name.mkdir.gwlpr._
import packets._
import events._

import SessionState.SessionState

class LoginServer(val port: Int) extends ServerTrait[LoginSession] {
    def initSession(socket: SocketHandle) = LoginSession(socket)
    val sessions : HashMap[UUID, LoginSession] = HashMap.empty

    def deserialiserForState(state: SessionState) : Deserialiser = state match {
        case SessionState.New => unenc.Deserialise
        case SessionState.Accepted => c2l.Deserialise
    }

    override def preStart = {
        super.preStart
    }
}
