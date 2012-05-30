package name.mkdir.gwlpr.login

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr._

import akka.actor.IO.SocketHandle

import com.eaio.uuid.UUID

import scala.collection.mutable.HashMap

import java.nio.ByteBuffer

class LoginServer(val port: Int) extends ServerTrait[LoginSession] with DummyHandler {
    def initSession(socket: SocketHandle) = LoginSession(socket)
    val sessions : HashMap[UUID, LoginSession] = HashMap.empty

    def clientConnected(session: LoginSession) = {}
    def clientDisconnected(session: LoginSession) = {}

    def clientMessage(session: LoginSession, buffer: ByteBuffer) = {
       val packets = deserialisePackets(session, buffer, session.state match {
            case SessionState.New => unenc.Deserialise
            case SessionState.Accepted => c2l.Deserialise
         })

       packets.foreach(handlePacket(session).apply(_))
    }

}
