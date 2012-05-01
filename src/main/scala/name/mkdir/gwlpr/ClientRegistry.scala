package name.mkdir.gwlpr

import scala.collection.mutable.HashMap

import com.eaio.uuid.UUID

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import name.mkdir.gwlpr.login.LoginServer


class ClientRegistry(port: Int) extends Actor {
  import IO._

  val system = ActorSystem()
  val sessions = new HashMap[UUID, Session]
  val loginServer = system.actorOf(Props[LoginServer], name="login")

  val loginSerializer = SerializationExtension(ActorSystem()).serializerFor(classOf[LoginPacket]) 
  val gameSerializer = None //TODO: SerializationExtension(ActorSystem()).serializerFor(classOf[GamePacket]) 

  override def preStart {
    IOManager(context.system) listen new InetSocketAddress(port)
  }

  def receive = {

    case NewClient(server) => {
      val socket = server.accept()
      sessions += (socket.uuid -> Session(socket))  
    }

    case Read(socket, bytes) => {
      val session = sessions(socket.uuid)

      if(!session.isLoggedIn) {
        loginServer ! MessageEvent(session, loginSerializer.fromBinary(bytes.toArray, manifest = None).asInstanceOf[List[Packet]])
      }
      else
        println("oops")
    }

    case Closed(socket, cause) =>
    sessions -= socket.uuid
  }
}

