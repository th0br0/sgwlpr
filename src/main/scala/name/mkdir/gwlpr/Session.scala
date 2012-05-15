package name.mkdir.gwlpr

import com.eaio.uuid.UUID
import akka.actor.IO.SocketHandle
import akka.util.ByteString

import login.LoginSession
import scala.util.Random


case class Session(
    var socket: SocketHandle // nasty, but this value changes in between transitions.
  )
{
    val uuid = socket.uuid

    var isLoggedIn = false

// Should these really be val's?
    val loginSession = LoginSession()
//  val gameSession = GameSession()

    val securityKey1 = new Array[Byte](4)
    val securityKey2 = new Array[Byte](4)

    {
        val rnd = Random
        rnd.nextBytes(securityKey1)
        rnd.nextBytes(securityKey2)
    }

    def write(b: ByteString) = socket.write(b)
}




