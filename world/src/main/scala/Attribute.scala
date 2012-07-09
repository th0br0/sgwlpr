package sgwlpr.world


abstract class BaseAttribute[T](protected var _value: T)(implicit val owner: Entity) {
  def value: T = _value
}

abstract class ConstAttribute[T](initialValue: T)(implicit _owner: Entity) extends BaseAttribute[T](initialValue)
abstract class Attribute[T](initialValue: T)(implicit _owner: Entity) extends BaseAttribute[T](initialValue) {
  def value_=(newval: T) = {
    _value = newval
    emitValueChanged()
  }

  protected def emitValueChanged()
}
