package name.mkdir.gwlpr

import packets._


trait PacketHandler[T <: Session] extends Logging  {
    def handlePacket(session: T)() : PartialFunction[Packet, Unit]  = {
        case p: Packet => log.warning("%s was not handled".format(p))
    }
}

