package name.mkdir.gwlpr

import java.nio.ByteBuffer
import java.util.Random

import scala.collection.mutable.HashMap

import akka.util.ByteString
import akka.actor.IO.SocketHandle
import akka.actor.ActorLogging

import com.eaio.uuid.UUID

import packets.Packet

trait ProvidesSession[T <: Session] {
    def sessions : HashMap[UUID, T]
    def initSession(socket: SocketHandle) : T
    // XXX - Which other methods should be exposed?
    //    def deleteSession(uuid: UUID) : Boolean
}

abstract case class Session (
        socket: SocketHandle
    ){

    object State extends Enumeration {
        type State = Value
        val New, Accepted = Value
    }
    import State._

    def write(b: ByteString) = socket.write(b)
    def write(buf: ByteBuffer) = socket.write(ByteString(buf))
    def write(b: Array[Byte]) = socket.write(ByteString(b))

    def write(p: Packet) : Unit = write(p.toBytes)
    def write(p: List[Packet]) : Unit = write(p.map(_.toBytes).reduceLeft(_ ++ _))

    def uuid = socket.uuid

    var state: State = New

    var buffer: Option[ByteBuffer] = None
    val securityKeys : List[Array[Byte]] = List(new Array[Byte](4), new Array[Byte](4))

    {
        val rnd = new Random
        rnd.nextBytes(securityKeys(0))
        rnd.nextBytes(securityKeys(1))
    }
}
