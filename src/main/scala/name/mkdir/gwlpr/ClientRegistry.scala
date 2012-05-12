package name.mkdir.gwlpr

import scala.collection.mutable.HashMap

import com.eaio.uuid.UUID

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import name.mkdir.gwlpr.login.LoginServer


trait ClientRegistry extends Actor with ActorLogging {
  import IO._

  def port: Int
  
  lazy val socketAddress = new InetSocketAddress(port)

  val sessions = new HashMap[UUID, Session]

  def clientConnected(session: Session)
  def clientDisconnected(session: Session)
  def clientMessage(session: Session, data: Array[Byte])
  override def preStart {
    IOManager(context.system) listen socketAddress
  }

  def receive = {

    case NewClient(server) => 
        val socket = server.accept()
        sessions += (socket.uuid -> Session(socket))
        clientConnected(sessions(socket.uuid))

    case Read(socket, bytes) => 
        log.debug("--------------------------------------------------------------------------------------")
        log.debug("Incoming: " + bytes)
       clientMessage(sessions(socket.uuid), bytes.toArray) 
    
    case Closed(socket, cause) =>
      clientDisconnected(sessions(socket.uuid))

      sessions -= socket.uuid

  }
}

