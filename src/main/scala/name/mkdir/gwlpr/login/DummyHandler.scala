package name.mkdir.gwlpr.login

import name.mkdir.gwlpr._
import packets._

trait DummyHandler extends PacketHandler[LoginSession] {
    abstract override def handlePacket(session: LoginSession)() : PartialFunction[Packet, Unit] = {
      case f: c2l.Packet1 => {
        log.debug("Computer Info!")
        log.debug(f.unknown0)
        log.debug(f.unknown1)
      }
      case p => super.handlePacket(session).apply(p)
    }
}
