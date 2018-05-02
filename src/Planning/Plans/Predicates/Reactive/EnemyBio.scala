package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Terran

class EnemyBio extends Plan {
  
  description.set("Is the enemy threatening Terran Bio?")
  
  override def isComplete: Boolean = {
    With.units.countEnemy(Terran.Marine)    > 8   ||
    With.units.countEnemy(Terran.Barracks)  > 1   ||
    With.units.countEnemy(Terran.Medic)     > 1   ||
    With.units.countEnemy(Terran.Firebat)   > 1   ||
    With.enemies.exists(_.hasTech(Terran.Stim))   ||
    With.enemies.exists(_.getUpgradeLevel(Terran.MarineRange) > 0)
  }
}
