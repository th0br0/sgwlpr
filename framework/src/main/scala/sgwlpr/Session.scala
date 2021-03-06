package sgwlpr

import java.nio.ByteBuffer
import java.util.Random

import scala.collection.mutable.HashMap

import akka.util.ByteString
import akka.actor.IO.SocketHandle
import akka.actor.ActorLogging

import com.eaio.uuid.UUID

import packets.Packet

case class SessionTransit[T <: Session](session: T)

trait ProvidesSession[T <: Session] {
  def sessions : HashMap[UUID, T]
  def initSession(socket: SocketHandle) : T
  // XXX - Which other methods should be exposed?
  //    def deleteSession(uuid: UUID) : Boolean
}

object SessionState extends Enumeration {
  type SessionState = Value
  val New, Accepted = Value
}

trait Session {

  import SessionState._

  def socket: SocketHandle

  def write(b: ByteString) = socket.write(b)
  def write(buf: ByteBuffer) = socket.write(ByteString(buf))
  def write(b: Array[Byte]) = socket.write(ByteString(b))
  def write(p: Packet) : Unit = write(p.toBytes)
  def write(p: List[Packet]) : Unit = write(p.map(_.toBytes).reduceLeft(_ ++ _))

  // XXX - emit event
  def drop() = socket.close

  def uuid = socket.uuid

  var state: SessionState = New
  var seed: List[Byte] = Nil

  var buffer: Option[ByteBuffer] = None

  var securityKeys : List[Int] = 
  {
    val rnd = new Random
    List(
      rnd.nextInt,
      rnd.nextInt
    )
  }
}
