package name.mkdir.gwlpr

import akka.actor._
import java.nio.{ByteBuffer, ByteOrder}
import java.net.InetSocketAddress


import scala.collection.mutable.HashMap

trait ServerTrait[T <: Session] extends Actor with ActorLogging with ProvidesSession[T]{
    import IO._
    
    /** the port to listen on */
    def port: Int
    // XXX - should we also specify the listen host manually?
    lazy val socketAddress = new InetSocketAddress(port)

    override def preStart {
        IOManager(context.system) listen socketAddress
    }

    def clientConnected(session: T)
    def clientDisconnected(session: T)
    def clientMessage(session: T, buffer: ByteBuffer)

    def receive = {
      case NewClient(server) => {
        // XXX - eventually, add some connection limitation here
        val socket = server.accept()
        log.info("Accepted new client with UUID: " + socket.uuid)
        
        // Construct new session and add it to the hashmap
        val session = initSession(socket)
        sessions += socket.uuid -> session

        clientConnected(session)
      }

      case Read(socket, byteString) => clientMessage(sessions(socket.uuid), byteString.toByteBuffer.order(ByteOrder.LITTLE_ENDIAN))
      case Closed(socket, cause) => {
        log.info("Client(UUID: %s) lost. Reason: %s".format(socket.uuid, cause))

        clientDisconnected(sessions(socket.uuid))

        sessions -= socket.uuid
      }
    }

}
