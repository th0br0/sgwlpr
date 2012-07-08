package sgwlpr.outpost

import sgwlpr.packets._
import sgwlpr.events._

import g2c._

class ClientAcceptedHandler extends Handler {

  def handleClientAccepted(event: ClientAccepted) = {
    val session = event.session.asInstanceOf[OutpostSession]

    log.debug("w00t!")
    session.write(List(
      // XXX - the instanceloadheader values should depend on our mapId!
      new InstanceLoadHeaderPacket(0x3F, 0x3F),
      new InstanceLoadCharacterNamePacket(session.character.get.name.get),
      // XXX - really, this information should be passed to the handler upon creation or so...
      new InstanceLoadDistrictInfoPacket(
        characterId = 1, // just wtf is this for?
        mapId = 148,
        isOutpost = 1, // XXX - create an implicit "Boolean" => byte
        districtRegion = 0x00010001, // XXX - find some decent way to serialise the districtRegion
        language = 0x01, // XXX - find more information on the various values here?
        isObserver = 0x00
      )
    ))

  }


  addEventHandler(manifest[ClientAccepted], handleClientAccepted)
}
