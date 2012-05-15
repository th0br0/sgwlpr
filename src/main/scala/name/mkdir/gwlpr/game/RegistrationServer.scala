package name.mkdir.gwlpr.game

import java.net.InetSocketAddress

import scala.collection.mutable.HashMap

import akka.actor._
import akka.event.Logging
import akka.util.{ ByteString, ByteStringBuilder }
import akka.serialization._
import akka.pattern.ask

import com.eaio.uuid.UUID

import name.mkdir.gwlpr._

class RegistrationServer(val port: Int) extends ClientRegistry {

  val serializer = SerializationExtension(context.system).serializerFor(classOf[GamePacket])

  def clientConnected(session: Session) = {}
  def clientDisconnected(session: Session) = {}
  
  def clientMessage(session: Session, data: Array[Byte]) = 
    handlePackets(
        session,
        serializer.fromBinary(data, manifest = None).asInstanceOf[List[Packet]].filterNot(_ == PacketError)
      )

  // Events go in here.
  override def receive = eventHandler orElse super.receive
  
  def eventHandler : Receive = {
     case ClientTransferRequest(session) => sessions += (session.uuid -> session); sender ! ClientAccepted("127.0.0.1", port)
     case e: Event => log.debug("Received event"); println(e)
  }

  def handlePackets(session: Session, packets: List[Packet]) = {

    log.debug("Source: " + session.socket)
    log.debug("Packets: " + packets)


    //TODO: Optimise this.
    var outgoing : List[Packet] = Nil
    packets.foreach {
      case ClientVerificationPacket(_, _, _, securityKey1, _, securityKey2, accountHash, characterHash, _, _) => 

        // Find the old sesion.
        //TODO: is this the best way to do this?
        val oldsession = sessions.find{ case (k,v) => v.securityKey1.deep == securityKey1.deep && v.securityKey2.deep == securityKey2.deep }.get._2
        oldsession.socket = session.socket
        sessions -= oldsession.uuid
        sessions -= session.uuid
        sessions += (session.uuid -> oldsession)

      case ClientSeedPacket(seed) => 
        outgoing = ServerSeedPacket(Array[Byte](0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)) :: outgoing
        outgoing = InstanceLoadHeaderPacket(true) :: outgoing
        outgoing = Packet379() :: outgoing
      case _ => 
    }     

    outgoing = outgoing.reverse

    log.debug("Sent: " + outgoing.reverse)
    if(!outgoing.isEmpty)
      session.write(outgoing.map(p => ByteString(PacketParser.unapply(p))).foldLeft(ByteString())(_++_))

  }
}

