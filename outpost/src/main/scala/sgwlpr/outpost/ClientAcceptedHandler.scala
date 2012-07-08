package sgwlpr.outpost

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.DistrictInfo

import g2c._

class ClientAcceptedHandler(districtInfo: DistrictInfo) extends Handler {

  def handleClientAccepted(event: ClientAccepted) = {
    val session = event.session.asInstanceOf[OutpostSession]

    session.write(List(
      // XXX - the instanceloadheader values should depend on our mapId!
      new InstanceLoadHeaderPacket(0x3F, 0x3F),
      new InstanceLoadCharacterNamePacket(session.character.get.name.get),
      // XXX - really, this information should be passed to the handler upon creation or so...
      new InstanceLoadDistrictInfoPacket(
        characterId = 1, // just wtf is this for?
        mapId = 148,
        isOutpost = 1, // XXX - create an implicit "Boolean" => byte
        districtRegion = districtInfo.asInt, // XXX - find some decent way to serialise the districtRegion
        language = districtInfo.language, // XXX - find more information on the various values here?
        isObserver = 0x00
      )
    ))

  }


  addEventHandler(manifest[ClientAccepted], handleClientAccepted)
}
