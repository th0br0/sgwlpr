package name.mkdir.gwlpr.login

import com.eaio.uuid.UUID
import scala.collection.mutable.HashMap

import akka.actor._
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress
import akka.serialization._

import c2s._
import s2c._

import name.mkdir.gwlpr._

class LoginServer extends Actor {
  import IO._

  val sessions = new HashMap[UUID, Session]()

  def receive = {
    case NewClientEvent(session) => sessions += (session.uuid -> session)
    case MessageEvent(session, packets) => packets foreach {handlePacket(session, _)}
    case _ => println("weird... this shouldn't be happening...")

  }

  // We should probably collect all outgoing packets in some queue first... but then, we *are* doing that as write sends a message to the IOManager after all!
  def handlePacket(uuid: UUID, packet: Packet) = {


    val session = sessions(uuid)

    println("---------------------------------------------------------------------")
    println("Source: " + session.socket)
    println("Packet: " + packet)


    //TODO: Optimize this.
    var outgoing : List[Packet] = Nil
    packet match {
      case ClientSeedPacket(seed) => 
        outgoing = ServerSeedPacket(Array[Byte](0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)) :: outgoing
      case ComputerInfoPacket(user, hostname) => 
        outgoing = ComputerInfoReply(loginCount = 0) :: outgoing
      case ResponseRequestPacket(loginCount) =>
        session.loginSession.syncCount = loginCount
        outgoing = 
          StreamTerminatorPacket(loginCount) ::
          ResponseRequestReply(loginCount) :: 
          outgoing
      case p @ AccountLoginPacket(syncCount, _, email, _, charName) => 
        session.loginSession.syncCount = syncCount
        
        session.loginSession.email = email
        session.loginSession.password = p.passwordString
        session.loginSession.charName = charName

        println(sessions(uuid).loginSession)

        outgoing = StreamTerminatorPacket(syncCount, Error.None) ::
            AccountPermissionsPacket(syncCount) ::
            FriendsListEndPacket(syncCount) ::
            AccountGuiSettingsPacket(syncCount, Array(0)) :: outgoing


        outgoing ::= StreamTerminatorPacket(syncCount, Error.None)
      case LogoutPacket(_) => 
        println("TODO: clean up the ClientRegistry session map every once in a while")
        sessions -= uuid
      case _ => 
    }     

    outgoing = outgoing.reverse

    println("Sent: " + outgoing.reverse)
    if(!outgoing.isEmpty)
      session.write(outgoing.map(p => ByteString(PacketParser.unapply(p))).foldLeft(ByteString())(_++_))

  }
}

