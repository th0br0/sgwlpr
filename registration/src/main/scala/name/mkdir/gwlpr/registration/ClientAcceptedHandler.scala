package sgwlpr.registration

import sgwlpr.packets._
import sgwlpr.events._

import g2c._

class ClientAcceptedHandler extends Handler {

  def handleClientAccepted(event: ClientAccepted) = {
    val session = event.session

    session.write(List(
      new InstanceLoadHeaderPacket(0x3F, 0x3F),
      new Packet379
    ))

  }


  addEventHandler(manifest[ClientAccepted], handleClientAccepted)
}
