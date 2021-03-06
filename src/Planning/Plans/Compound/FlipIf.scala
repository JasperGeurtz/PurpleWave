package Planning.Plans.Compound

import Planning.Plans.Basic.NoPlan
import Planning.Predicates.Never
import Planning.{Plan, Predicate, Property}

class FlipIf(
  initialPredicate  : Predicate = new Never,
  inititialFirst    : Plan = NoPlan(),
  initialSecond     : Plan = NoPlan())
    extends Plan {
  
  description.set("Flip if")
  
  val predicate = new Property[Predicate](initialPredicate)
  val first  = new Property[Plan](inititialFirst)
  val second = new Property[Plan](initialSecond)
  
  override def getChildren: Iterable[Plan] = Vector(first.get, second.get)
  override def isComplete: Boolean = predicate.get.isComplete && first.get.isComplete
  
  override def onUpdate() {
    if (predicate.get.isComplete) {
      delegate(second.get)
      delegate(first.get)
    }
    else {
      delegate(first.get)
      delegate(second.get)
    }
  }
}
