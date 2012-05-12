package name.mkdir.gwlpr

import com.eaio.uuid.UUID
import akka.actor.IO.SocketHandle
import akka.util.ByteString

import login.LoginSession

case class Session(
    var socket: SocketHandle // nasty, but this value changes in between transitions.
  )
{
    val uuid = socket.uuid

    var isLoggedIn = false

// Should these really be val's?
    val loginSession = LoginSession()
//  val gameSession = GameSession()

    def write(b: ByteString) = socket.write(b)
}




