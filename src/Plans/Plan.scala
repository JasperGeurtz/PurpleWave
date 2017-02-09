package Plans

import Types.Property

class Plan {
  val description = new Property[Option[String]](None)
  
  def isComplete:Boolean = { false }
  def getChildren:Iterable[Plan] = { List.empty }
  def onFrame() = {}
  def drawOverlay() = { }
  
  override def toString: String = { this.getClass.getName ++ description.get.getOrElse("") }
}