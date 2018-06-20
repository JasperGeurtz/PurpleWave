package Planning

class Property[T](default: T) {
  
  private var _parent: Option[Property[T]] = None
  private var _value: T = default
  
  def get: T = {
    if (_parent.contains(this)) {
      throw new Exception("Cyclical inheritance for " + this.getClass.toString)
    }
    _parent.map(_.get).getOrElse(_value)
  }
  
  def inherit(parent: Property[T]) {
    if (parent == this) {
      throw new Exception("Tried to assign cyclical inheritance for " + this.getClass.toString)
    }
    
    _parent = Some(parent)
  }
  
  def apply(): T = get
  
  def reset() {
    set(default)
  }
  
  def set(value: T) {
    _value = value
  }
  
  override def toString: String = "Property: " + get.toString
}
