package sgwlpr

import com.eaio.uuid.UUID
import scala.collection.mutable.ListBuffer
import events._
import packets._

trait GameServerTrait[T <: Session] extends ServerTrait[T] {
  val sessionsInTransit : ListBuffer[Session] = ListBuffer.empty

  def migrateSession(oldSession: Session, newSession: T) : Boolean

  override def handlePacket(event: Event) = event match {
    case evt: unenc.ClientVerificationPacketEvent => {
      val session = evt.session.asInstanceOf[T]
      val packet = evt.packet
      val providedKeys = List(packet.securityKey1, packet.securityKey2)

      // XXX - what if we can't find an old session
      val oldSession = sessionsInTransit.find(_.securityKeys == providedKeys)

      if(oldSession == None || !migrateSession(oldSession.get, session))
        session.drop
    }
      
    case e => super.handlePacket(e)
  }

  override def receive = {
    case SessionTransit(session) => sessionsInTransit += session
    case m => super.receive(m)
  }
}
