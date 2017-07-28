package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetAnything extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight          &&
    unit.agent.toAttack.isEmpty  &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
  }
}
