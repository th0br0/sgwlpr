package name.mkdir.gwlpr

import scala.collection.mutable.HashMap

import com.eaio.uuid.UUID

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import name.mkdir.gwlpr.login.c2s._
import name.mkdir.gwlpr.login.s2c._

class ClientRegistry(port: Int) extends Actor {
  import IO._

  val sessions = new HashMap[UUID, Session]

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

      println("---------------------------------------------------------------------")
      println("Length: " + bytes.size)
      println("Content: " + bytes)
      println("Socket: " + socket)
      
      val packets = {
        if(session.isLoggedIn)
          loginSerializer.fromBinary(bytes.toArray, manifest = None)
        else
          Nil
      }

      println("Result: " + packets)


      //TODO: Optimize this.
      var outgoing : List[Packet] = Nil
      packets.asInstanceOf[List[Packet]].foreach {
            case ClientSeedPacket(seed) => 
                    outgoing = ServerSeedPacket(Array[Byte](0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)) :: outgoing
            case ComputerInfoPacket(user, hostname) => 
                    outgoing = ComputerInfoReply(loginCount = 0) :: outgoing
            case ResponseRequestPacket(loginCount) =>
                    outgoing = 
                        StreamTerminatorPacket(loginCount) ::
                        ResponseRequestReply(loginCount) :: outgoing
            case _ => 
      }        
      outgoing = outgoing.reverse


      println("Sent: " + outgoing.reverse)
      if(!outgoing.isEmpty)
        // FIXME: use PacketParser directly here?
        session.socket.write(outgoing.map(p => ByteString(loginSerializer.toBinary(p))).foldLeft(ByteString())(_++_))

      //        state(socket)(Chunk(bytes))
    }

    case Closed(socket, cause) =>
      sessions -= socket.uuid
    }
}

