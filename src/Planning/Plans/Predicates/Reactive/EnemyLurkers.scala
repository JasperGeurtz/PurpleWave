package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class EnemyLurkers extends Predicate {
  
  description.set("Is the enemy threatening Lurkers?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(_.is(Zerg.Lurker)) ||
    With.units.enemy.exists(_.is(Zerg.LurkerEgg)) ||
    (
      (
        With.units.enemy.exists(_.is(Zerg.Hydralisk)) ||
        With.units.enemy.exists(_.is(Zerg.HydraliskDen))
      )
      && With.units.enemy.exists(_.is(Zerg.Lair))
    )
  }
}
