$classdoc$
case class $className$($attributes; separator=", "$) extends Packet($header$) {
    def size : Int = $size$

    $assertions; separator="\n"$

    $nestedClasses; separator="\n"$

    $serialisedoc$
    $serialise$
}

/**
  * Companion object for $className$
  */
object $className$ {
    $deserialisedoc$
    $deserialise$
}
