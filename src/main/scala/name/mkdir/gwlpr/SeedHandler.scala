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

    def handleClientSeed(session: Session, seed: List[Byte]) : Unit = {
        log.debug("Session: " + session.hashCode)
        session.seed = seed
        session.state = SessionState.Accepted
        
        session.write(new ServerSeedPacket(seed))

        log.debug("Handled ClientSeedPacket; " + seed)
    }

    def receive = {
        case c: ClientSeedEvent => handleClientSeed(c.session, c.packet.seed)
    }

    subscribeTo(classOf[ClientSeedEvent])
}
