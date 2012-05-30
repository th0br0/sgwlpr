package name.mkdir.gwlpr

import java.util.Random

import packets._
import unenc._

trait SeedHandler[T <: Session] extends PacketHandler[T] {
    private val seed: List[Byte] = {
        val arr = new Array[Byte](64)
        (new Random).nextBytes(arr)
        arr.toList
    }

    abstract override def handlePacket(session: T)() : PartialFunction[Packet, Unit] = {
        case c: ClientSeedPacket => log.info("Handled ClientSeed."); session.write(new ServerSeedPacket(seed))
        case other => println("oops! " + other); super.handlePacket(session).apply(other)
    }


}