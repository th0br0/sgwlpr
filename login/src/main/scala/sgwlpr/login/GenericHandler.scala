package sgwlpr.login

import java.util.Random

import sgwlpr.packets._
import sgwlpr.events._

import c2l._
import l2c._

class GenericHandler extends Handler {
  def handleComputerInfo(session: LoginSession, packet: ComputerInfoPacket) = {
    log.debug("Received ComputerInfo(%s, %s)".format(packet.username, packet.hostname))

    session.write(new ComputerInfoReplyPacket(heartbeat = session.heartbeat))
  }

  def handleResponseRequest(session: LoginSession, packet: ResponseRequestPacket) = {
    log.debug("Received ResponseRequest")
    session.heartbeat = packet.heartbeat

    session.write(new ResponseRequestReplyPacket(heartbeat = session.heartbeat))
    session.write(new StreamTerminatorPacket(heartbeat = session.heartbeat))
  }

  def handleExit(session: LoginSession, packet: ExitPacket) = {
    // TODO: Turn ErrorCode into an Enumeration
    log.debug("Reason for disconnection: " + packet.exitCode)
  }

  def handlePacket32(session: LoginSession, packet: c2l.Packet32) = {
    session.heartbeat = packet.heartbeat
    session.write(new StreamTerminatorPacket(heartbeat = session.heartbeat))
  }

  def handlePacket33(session: LoginSession, packet: c2l.Packet33) = {
    session.heartbeat = packet.heartbeat
  }

  addMessageHandler(manifest[ComputerInfoPacketEvent], handleComputerInfo)
  addMessageHandler(manifest[ResponseRequestPacketEvent], handleResponseRequest)
  addMessageHandler(manifest[ExitPacketEvent], handleExit)

  addMessageHandler(manifest[c2l.Packet32Event], handlePacket32)
  addMessageHandler(manifest[c2l.Packet33Event], handlePacket33)
}
