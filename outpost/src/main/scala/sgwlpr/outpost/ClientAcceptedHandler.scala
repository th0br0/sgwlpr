package sgwlpr.outpost

import sgwlpr.packets._
import sgwlpr.events._
import sgwlpr.DistrictInfo

import g2c._
import sgwlpr.world.{RegisterPlayer, Player, Registered}

import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask

// XXX - move this to Server? yes?
class ClientAcceptedHandler(mapId: Int, districtInfo: DistrictInfo) extends Handler {
  val world = context.actorFor("../world")

  def handleClientAccepted(event: ClientAccepted) = {
    val session = event.session.asInstanceOf[OutpostSession]
  
    implicit val timeout = Timeout(2 seconds)

    (world ? RegisterPlayer(session, session.character.get, session.inventory.get)) onSuccess {
      case Registered(id, ref) => 
        session.player = Some(ref)
        session.write(List(
          // XXX - the instanceloadheader values should depend on our mapId!
          new InstanceLoadHeaderPacket(0x3F, 0x3F),
          new InstanceLoadCharacterNamePacket(session.character.get.name.get),
          // XXX - really, this information should be passed to the handler upon creation or so...
          new InstanceLoadDistrictInfoPacket(
            characterId = id, // just wtf is this for?
            mapId = mapId,
            isOutpost = 1, // XXX - create an implicit "Boolean" => byte
            districtRegion = districtInfo.asInt, // XXX - find some decent way to serialise the districtRegion
            language = districtInfo.language, // XXX - find more information on the various values here?
            isObserver = 0x00
          )
        ))
    }
  }


  addEventHandler(manifest[ClientAccepted], handleClientAccepted)
}
