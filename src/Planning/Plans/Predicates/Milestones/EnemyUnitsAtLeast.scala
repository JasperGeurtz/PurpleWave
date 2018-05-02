package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}
import Planning.Plan

class EnemyUnitsAtLeast(
  quantity  : Int         = 0,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false)
  
  extends Plan {
  
  description.set("Enemy has at least " + quantity + " " + matcher)
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countEnemy(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else {
        With.units.countEnemy(matcher)
      }
    val output = quantityFound >= quantity
    output
  }
}
