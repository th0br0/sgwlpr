package $package$.$dir$

$classdoc$
case class $className$($attributes; separator=", "$) extends Packet($header$) {
    def size : Int = $size$

    $assertions; separator="\n"$

    $nestedClasses; separator="\n"$

    $serialisedoc$
    $serialise$
    
    $deserialisedoc$
    $deserialise$
}

