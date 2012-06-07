package name.mkdir.gwlpr.registration

import name.mkdir.gwlpr.packets._
import name.mkdir.gwlpr.events._

import g2c._

class ClientAcceptedHandler extends Handler {

  def handleClientAccepted(event: ClientAccepted) = {
    val session = event.session

    log.debug("client accepted -- yay --  " + session.state)
  }


  addEventHandler(manifest[ClientAccepted], handleClientAccepted)
}
