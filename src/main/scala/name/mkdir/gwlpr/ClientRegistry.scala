package name.mkdir.gwlpr

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import name.mkdir.gwlpr.packets._

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
      val packet = serializer.fromBinary(bytes.toArray, manifest = None)
      println("Result: " + packet)
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
