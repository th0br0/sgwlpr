package name.mkdir.gwlpr.login

import java.net.InetSocketAddress

import scala.collection.mutable.HashMap

import akka.actor._
import akka.event.Logging
import akka.util.{ ByteString, ByteStringBuilder, Timeout }
import akka.util.duration._
import akka.serialization._
import akka.pattern.ask

import com.eaio.uuid.UUID

import name.mkdir.gwlpr._

class LoginServer(val port: Int) extends ClientRegistry {
  import IO._

  val serializer = SerializationExtension(context.system).serializerFor(classOf[LoginPacket])
  lazy val registrationServer = context.actorFor("../registration")

  def clientConnected(session: Session) = {}
  def clientDisconnected(session: Session) = {}
  
  def clientMessage(session: Session, data: Array[Byte]) = 
    handlePackets(
        session,
        serializer.fromBinary(data, manifest = None).asInstanceOf[List[Packet]].filterNot(_ == PacketError)
      )

  
  def handlePackets(session: Session, packets: List[Packet]) = {

    log.debug("Source: " + session.socket)
    log.debug("Packets: " + packets)


    //TODO: Optimise this.
    var outgoing : List[Packet] = Nil
    packets.foreach {
      case ClientSeedPacket(seed) => 
        outgoing = ServerSeedPacket(Array[Byte](0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)) :: outgoing
      case ComputerInfoPacket(user, hostname) => 
        outgoing = ComputerInfoReply(syncCount = session.loginSession.syncCount) :: outgoing
      case ResponseRequestPacket(syncCount) =>
        session.loginSession.syncCount = syncCount
        outgoing = 
          StreamTerminatorPacket(syncCount) ::
          ResponseRequestReply(syncCount) :: 
          outgoing
      case p @ AccountLoginPacket(syncCount, _, email, _, charName) => 
        //TODO: branch this out properly somewhere... some trait maybe?
        session.loginSession.syncCount = syncCount
        
        session.loginSession.email = email
        session.loginSession.password = p.passwordString
        session.loginSession.charName = charName

        outgoing = StreamTerminatorPacket(syncCount, Error.None) ::
            AccountPermissionsPacket(syncCount) ::
            FriendsListEndPacket(syncCount) ::
            AccountGuiSettingsPacket(syncCount, Array(0)) :: outgoing


        outgoing ::= StreamTerminatorPacket(syncCount, Error.None)
      case CharacterPlayPacket(syncCount, _, mapId, _, _, _) => 
        session.loginSession.syncCount = syncCount
        if(mapId == 0) { // Registration "map"
            log.debug(session.uuid + " requests transfer to registration server")
            
            implicit val timeout = Timeout(5 seconds) // TODO: FIXME. The value for this should be stored elsewhere. In the config?

            //TODO: FIXME: this is bad code.
            ask(registrationServer, ClientTransferRequest(session)) onSuccess {
                case ClientAccepted(ip, port) => 
                    log.debug("client got accepted at registration server at " + ip + ":" + port)

                    import java.nio.{ByteBuffer, ByteOrder}
                    val bb = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
                    bb.putShort(2)
                    bb.putShort(0xB11F.toShort)
                    bb.put(127:Byte); bb.put(0:Byte); bb.put(0:Byte); bb.put(1:Byte)

                    val serverInfo = bb.array()
                    val p = ReferToGameServerPacket(session.loginSession.syncCount, session.securityKey1, mapId, serverInfo, session.securityKey2)
                    session.write(ByteString(PacketParser.unapply(p)))
                case ClientRejected => session.write(ByteString(PacketParser.unapply(StreamTerminatorPacket(session.loginSession.syncCount, Error.NetworkError))))
            }
        }
      case LogoutPacket(_) => 
        log.warning("TODO: clean up the ClientRegistry session map every once in a while")
      case ExitPacket(exitCode) => 
      case _ => 
    }     

    outgoing = outgoing.reverse

    log.debug("Sent: " + outgoing.reverse)
    if(!outgoing.isEmpty)
      session.write(outgoing.map(p => ByteString(PacketParser.unapply(p))).foldLeft(ByteString())(_++_))

  }
}

