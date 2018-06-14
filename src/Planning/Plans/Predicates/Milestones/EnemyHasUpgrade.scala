package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Upgrades.Upgrade

class EnemyHasUpgrade(upgrade: Upgrade, level: Int = 1) extends Predicate {
  
  description.set("Enemy has an upgrade")
  
  override def isComplete: Boolean = With.enemies.exists(_.getUpgradeLevel(upgrade) >= level)
}
