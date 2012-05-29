$classdoc$

// XXX - Fix the case class 
class $className$($attributes; separator=", "$) extends Packet($header$) with Serializable {
    def size : Int = $size$

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

