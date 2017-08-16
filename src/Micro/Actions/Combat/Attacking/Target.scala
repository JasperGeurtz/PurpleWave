package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Target extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    TargetRelevant.delegate(unit)
    var canPillage = false
    canPillage ||= unit.agent.canPillage
    canPillage ||= unit.pixelCenter.zone.owner.isEnemy
    canPillage &&= unit.matchups.threatsInRange.isEmpty
    if (canPillage) {
      TargetAnything.delegate(unit)
    }
  }
}
