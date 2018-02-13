package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, Leave}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object FallBack extends ActionTechnique {
  
  // Shoot while retreating.
  // eg. Dragoons retreating from Vultures
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.unitClass.ranged
    && unit.matchups.targets.nonEmpty
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.is(Protoss.Dragoon)) return 1.0
    if (unit.is(Terran.Vulture)) return 1.0
    if (unit.is(Terran.SiegeTankUnsieged)) return 1.0
    0.0
  }
  
  override val applicabilityBase: Double = 0.50
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return Some(0.0)
    
    if (unit.topSpeed >= other.topSpeed) return Some(0.0)
    if (unit.pixelRangeAgainst(other) <= other.pixelRangeAgainst(unit)) return Some(0.0)
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Potshot.delegate(unit)
    Leave.delegate(unit)
  }
}
