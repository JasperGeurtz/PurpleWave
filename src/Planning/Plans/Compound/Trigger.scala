package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Trigger(
  trigger  : Plan = new Plan,
  after    : Plan = new Plan,
  before   : Plan = new Plan)
  extends Plan {
  
  description.set("When triggered")
  
  val predicate = new Property[Plan](trigger)
  val whenTrue  = new Property[Plan](after)
  val whenFalse = new Property[Plan](before)
  
  var triggered: Boolean = false
  
  override def getChildren: Iterable[Plan] = Vector(predicate.get, whenTrue.get, whenFalse.get)
  
  override def onUpdate() {
    delegate(predicate.get)
    triggered = triggered || predicate.get.isComplete
      
    if (triggered)
      delegate(whenTrue.get)
    else
      delegate(whenFalse.get)
  }
}
