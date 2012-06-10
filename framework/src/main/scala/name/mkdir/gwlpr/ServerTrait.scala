package name.mkdir.gwlpr

import akka.actor._
import java.nio.{ByteBuffer, ByteOrder}
import java.net.InetSocketAddress


import scala.collection.mutable.HashMap

import packets._
import events._

import SessionState.SessionState
import akka.event.EventStream

trait ServerTrait[T <: Session] extends Actor with ActorLogging with ProvidesSession[T] {
  import IO._

  /** the port to listen on */
  def port: Int
  // XXX - should we also specify the listen host manually?
  
  val eventStream = new EventStream

  override def preStart {
    import akka.actor.Props
    IOManager(context.system) listen (new InetSocketAddress(port))


  }

  // XXX - redesign this eventually...
  def handlePacket(event: Event) = event match {
    case c: unenc.ClientSeedPacketEvent => {
      c.session.write(new unenc.ServerSeedPacket(Iterator.fill(20)(0.toByte).toList))
      c.session.state = SessionState.Accepted

      eventStream.publish(ClientAccepted(c.session))
    }
    case evt => eventStream.publish(evt)
  }

  def deserialiserForState(state: SessionState) : Deserialiser
  def deserialisePackets(session: T, buffer: ByteBuffer, deserialise: Deserialiser) : List[Packet] = {
    // XXX - use a ListBuffer here
    var ret : List[Packet] = Nil

    val buf = {
      if(session.buffer == None)
        buffer
      else
        // XXX - this doesn't work. fix it (create a new bytebuffer and merge the two old ones)
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

      val packets = deserialisePackets(session, buffer, deserialiserForState(session.state))

      packets.foreach{ p => log.debug("received: " + p); handlePacket(p.toEvent(session)) }
    }
    case Closed(socket, cause) => {
      log.info("Client(UUID: %s) lost. Reason: %s".format(socket.uuid, cause))

      self ! ClientDisconnected(sessions(socket.uuid))

      sessions -= socket.uuid
    }

    case SubscribeToEvent(clazz) => {
      eventStream.subscribe(sender, clazz)
    }
    case _ =>
  }

}
