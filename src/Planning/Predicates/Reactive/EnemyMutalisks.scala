package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class EnemyMutalisks extends Predicate {
  
  override def isComplete: Boolean = {
    With.units.existsEnemy(Zerg.Mutalisk, Zerg.Spire)
  }
}
