package name.mkdir.gwlpr.login

import akka.actor.IO.SocketHandle

import com.eaio.uuid.UUID

import scala.collection.mutable.HashMap

import java.nio.ByteBuffer

import name.mkdir.gwlpr._
import packets._
import events._

class LoginServer(val port: Int) extends ServerTrait[LoginSession] with DummyHandler {
    def initSession(socket: SocketHandle) = LoginSession(socket)
    val sessions : HashMap[UUID, LoginSession] = HashMap.empty

    def deserialiserForState(state: SessionState) : Deserialiser = state match {
        case SessionState.New => unenc.Deserialise
        case SessionState.Accepted => unenc.Deserialise
    }

    def clientConnected(session: LoginSession) = {}
    def clientDisconnected(session: LoginSession) = {}

    def clientMessage(session: LoginSession, buffer: ByteBuffer) = {
    }

}
