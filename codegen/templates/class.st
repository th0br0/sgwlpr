$classdoc$

case class $className$Event(session: Session, packet: $className$) extends ClientMessageEvent
class $className$($attributes; separator=", "$) extends Packet($header$) with Serializable {
    def size : Int = $size$
    def toEvent(session: Session) : ClientMessageEvent = $className$Event(session, this)

    $assertions; separator="\n"$


    $serialisedoc$
    $serialise$
}

/**
  * Companion object for $className$
  */
object $className$ {
    $nestedClasses; separator="\n"$

    $deserialisedoc$
    $deserialise$
}

