//

/** Helper object for deserialising packets of this direction */
object Deserialise extends Deserialiser {
    def apply(buf: ByteBuffer) : Packet = {
	val header = buf.getShort

	header match {
	    $cases$
	    case _ => PacketError(header, getClass.getName)
	}
    }
}
