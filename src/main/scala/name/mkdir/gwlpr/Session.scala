package name.mkdir.gwlpr

import com.eaio.uuid.UUID
import akka.actor.IO.SocketHandle
import akka.util.ByteString

import login.LoginSession

case class Session(
    socket: SocketHandle
  )
{
    val uuid = socket.uuid

    var isLoggedIn = false

    var user : Option[UserData] = None   

// Should these really be val's?
    val loginSession = LoginSession()
//  val gameSession = GameSession()

    def write(b: ByteString) = socket.write(b)
}

case class UserData(
  email: String
)



