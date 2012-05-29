package name.mkdir.gwlpr

import com.eaio.uuid.UUID
import java.nio.ByteBuffer
import akka.util.ByteString

import java.util.Random

trait ProvidesSession[T >: Session] {
    def session(uuid: UUID) : T

    // XXX - Which other methods should be exposed?
    //    def deleteSession(uuid: UUID) : Boolean
}

abstract case class Session (
        socket: SocketHandle
    ){
    def write(b: ByteString) = socket.write(b)
    def write(buf: ByteBuffer) = socket.write(ByteString(buf))
    def write(b: Array[Byte]) = socket.write(ByteString(b))

    def uuid = socket.uuid

    val securityKeys = List(new Array[Byte](4), new Array[Byte](4))
    {
        val rnd = Random
        rnd.nextBytes(securityKeys(0))
        rnd.nextBytes(securityKeys(1))
    }
}
