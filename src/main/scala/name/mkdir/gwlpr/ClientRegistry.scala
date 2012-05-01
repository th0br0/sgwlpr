package name.mkdir.gwlpr

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.packets.c2s._
import name.mkdir.gwlpr.packets.s2c._

class ClientRegistry(port: Int) extends Actor {
  import IO._

  val state = IterateeRef.Map.async[IO.Handle]()(context.dispatcher)
  val serializer = SerializationExtension(ActorSystem()).serializerFor(classOf[Packet]) 

  override def preStart {
    IOManager(context.system) listen new InetSocketAddress(port)
  }

  def receive = {

    case NewClient(server) => {
      val socket = server.accept()
      println("serializer: " + serializer)
      state(socket) flatMap (_ => ClientRegistry.processPacket(socket))
    }

    case Read(socket, bytes) => {
      println(serializer)
      println("---------------------------------------------------------------------")
      println("Length: " + bytes.size)
      println("Content: " + bytes)
      
      val packets = serializer.fromBinary(bytes.toArray, manifest = None)
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
        socket.asSocket.write(outgoing.map(p => ByteString(serializer.toBinary(p))).foldLeft(ByteString())(_++_))

      //        state(socket)(Chunk(bytes))
    }

    case Closed(socket, cause) =>
      state(socket)(EOF(None))
      state -= socket
    }
}

object ClientRegistry {
  import IO._
  import java.nio.ByteOrder
  def processPacket(socket: IO.SocketHandle) : IO.Iteratee[Unit] = {
    repeat {
      for {
        headerByteString <- take(2)

      } yield {
        val header = headerByteString.asByteBuffer

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.flip()

        println("header: " + (header.getShort() & 0xFFFF))
      }
    }
  }
}
