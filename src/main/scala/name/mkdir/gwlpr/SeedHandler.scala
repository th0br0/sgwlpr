package name.mkdir.gwlpr

import java.util.Random

import packets._
import unenc._
import events._

class SeedHandler extends Handler {
    private val seed: List[Byte] = {
        val arr = new Array[Byte](64)
        (new Random).nextBytes(arr)
        arr.toList
    }

    def handleClientSeed(session: Session, packet: ClientSeedPacket) : Unit = {
        log.debug("Session: " + session.hashCode)
        session.seed = packet.seed
        session.state = SessionState.Accepted
        
        session.write(new ServerSeedPacket(seed))

        log.debug("Handled ClientSeedPacket; " + seed)
    }

    addMessageHandler(manifest[ClientSeedPacketEvent], handleClientSeed)
}
