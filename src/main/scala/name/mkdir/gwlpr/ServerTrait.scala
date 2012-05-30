package name.mkdir.gwlpr

import akka.actor._
import java.nio.{ByteBuffer, ByteOrder}
import java.net.InetSocketAddress


import scala.collection.mutable.HashMap

import packets._
import events._

trait ServerTrait[T <: Session] extends Actor with ActorLogging with ProvidesSession[T]  with SeedHandler[T] with EventHandler {
    import IO._
    
    /** the port to listen on */
    def port: Int
    // XXX - should we also specify the listen host manually?
    lazy val socketAddress = new InetSocketAddress(port)

    override def preStart {
        IOManager(context.system) listen socketAddress
    }

    def deserialiserForState(state: SessionState) : Deserialiser
    def deserialisePackets(session: T, buffer: ByteBuffer, deserialise: Deserialiser) : List[Packet] = {
       // XXX - use a ListBuffer here
       var ret : List[Packet] = Nil

       val buf = {
            if(session.buffer == None)
              buffer
            else
              session.buffer.get.put(buffer)
            }

       def parse(buf: ByteBuffer) : List[Packet] = {
            if(!buf.hasRemaining) return Nil

            val pos = buf.position
            val packet = deserialise(buf)
            if(packet.isInstanceOf[PacketError])
            {
                buf.position(pos)
                session.buffer = Some(buf.slice)
    
                log.debug(packet.toString)

                Nil
            } else
                List(packet) ::: parse(buf)
       }
        
       parse(buf)
    }

    def receive = {
      case NewClient(server) => {
        // XXX - eventually, add some connection limitation here
        val socket = server.accept()
        log.info("Accepted new client with UUID: " + socket.uuid)
        
        // Construct new session and add it to the hashmap
        val session = initSession(socket)
        sessions += socket.uuid -> session

        self ! ClientConnected(session)
      }

      case Read(socket, byteString) => {
       val buffer = byteString.toByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
       val session = sessions(socket.uuid)

       deserialisePackets(session, buffer, deserialiserForState(session.state).foreach(self ! ClientMessage(session, _))
       packets.foreach(self ! ClientMessage(session, _))
      }
      case Closed(socket, cause) => {
        log.info("Client(UUID: %s) lost. Reason: %s".format(socket.uuid, cause))

        self ! ClientDisconnected(sessions(socket.uuid))

        sessions -= socket.uuid
      }

      case e: Event => handleEvent(e)
    }

}
