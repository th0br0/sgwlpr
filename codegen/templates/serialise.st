def toBytes : Array[Byte] = {
    val buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
    buf.putShort(header)

    $content$

    buf.array()
}
