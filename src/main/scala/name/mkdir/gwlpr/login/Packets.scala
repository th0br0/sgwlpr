package name.mkdir.gwlpr.login

import name.mkdir.gwlpr.Config

import name.mkdir.gwlpr.PacketAnnotations._
import name.mkdir.gwlpr.Packet

// ------------------------------------------------
// ------------------- C 2 S ----------------------
// ------------------------------------------------

case class KeepAlivePacket(data1: Long) extends Packet(0) 

case class ComputerInfoPacket(user: String, hostname: String) extends Packet(1) 

case class AccountLoginPacket(syncCount: Long, @ArrayInfo(constSize = true, size = 24) password: Array[Byte], email: String, data2: String, charName: String) extends Packet(4) {
    lazy val passwordString = 
      // maxSize is < 0xFF anyway ;S
      new String(password.drop(2).take(password(0)), Config.charSet)
}

case class LogoutPacket(data1: Long) extends Packet(13)
case class ExitPacket(exitCode: Long) extends Packet(14)

case class ComputerHardwarePacket(data1: Array[Byte], @ArrayInfo(constSize = true, size = 16) data2: Array[Byte]) extends Packet(15)

case class ClientIDPacket(data1: Long) extends Packet(35)

case class CharacterPlayPacket(syncCount: Long, data2: Long, gameMapId: Long, data4: Long, data5: Long, data6: Long) extends Packet(41)

case class ResponseRequestPacket(syncCount: Long) extends Packet(53)






// --------------- UNKNOWN PACKETS ----------------
case class Packet19(data1: Long, data2: Long) extends Packet(19)

// ------------------------------------------------
// ------------------- S 2 C ----------------------
// ------------------------------------------------


case class ComputerInfoReply(staticData: Long = 0x71953D3D, syncCount: Long, data3: Long = 0, data4: Long = 1) extends Packet(1)

case class StreamTerminatorPacket(syncCount: Long, errorCode: Long = 0) extends Packet(3)

case class CharacterInfoPacket(syncCount: Long, @ArrayInfo(constSize = true, size = 16) staticHash1 : Array[Byte] = new Array[Byte](16), staticData: Long = 0, charName: String, appearance: Array[Byte]) extends Packet(7)

case class ReferToGameServerPacket(syncCount: Long, @ArrayInfo(constSize = true, size = 4) securityKey1: Array[Byte], mapId: Long, @ArrayInfo(constSize = true, size = 24) serverInfo: Array[Byte], @ArrayInfo(constSize = true, size = 4) securityKey2: Array[Byte]) extends Packet(9)

case class ResponseRequestReply(syncCount: Long, data1: Long = 0) extends Packet(38)


case class AccountGuiSettingsPacket(syncCount: Long, rawData: Array[Byte]) extends Packet(22)

case class FriendsListEndPacket(syncCount: Long, staticData: Long = 1) extends Packet(20)

case class AccountPermissionsPacket(syncCount: Long,
                                    territory: Long = 2,
                                    territoryChanges: Long = 4,
                                    @ArrayInfo(constSize = true, size = 8)
                                    data1: Array[Byte] = List(0x3F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte).toArray,
                                    @ArrayInfo(constSize = true, size = 8)
                                    data2: Array[Byte] = List(0x80, 0x3F, 0x02, 0x00, 0x03, 0x00, 0x08, 0x00).map(_.toByte).toArray,
                                    @ArrayInfo(constSize = true, size = 16)
                                    data3: Array[Byte] = List(0x37, 0x4B, 0x09, 0xBB, 0xC2, 0xF6, 0x74, 0x43, 0xAA, 0xAB, 0x35, 0x4D, 0xEE, 0xB7, 0xAF, 0x08).map(_.toByte).toArray,
                                    @ArrayInfo(constSize = true, size = 16)
                                    data4: Array[Byte] = List(0x55, 0xB6, 0x77, 0x59, 0x0C, 0x0C, 0x15, 0x46, 0xAD, 0xAA, 0x33, 0x43, 0x4A, 0x91, 0x23, 0x6A).map(_.toByte).toArray,
                                    changeAccSettings: Long = 8,
                                    addedKeys: Array[Byte] = Array(0x01, 0x00, 0x06, 0x00, 0x57, 0x00, 0x01, 0x00),
                                    eulaAccepted: Byte = 23,
                                    data5: Long = 0) extends Packet(17)
