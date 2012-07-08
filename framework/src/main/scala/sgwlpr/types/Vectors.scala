package sgwlpr.types

// XXX - Write or use a proper vector implementation!
sealed trait Vector {
  def toList : List[Float] 
}

case class Vector2(x: Float, y: Float) extends Vector {
  def toList = List(x, y)
}

case class Vector3(x: Float, y: Float, z: Float) extends Vector {
  def toList = List(x, y, z)
}

case class Vector4(x: Float, y: Float, z: Float, a: Float) extends Vector {
  def toList = List(x, y, z, a)
}
