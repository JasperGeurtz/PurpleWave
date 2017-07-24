package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Kite
import Micro.Actions.Commands.Attack
import Micro.Behaviors.MovementProfiles
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object ProtectTheWeak extends Action {
  
  // Protect our workers from harassment. Don't abandon them!
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.matchups.threatsViolent.isEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val currentBullies = bullies(unit)
    
    if (currentBullies.nonEmpty) {
  
      unit.action.canCower = false
      unit.action.toAttack = EvaluateTargets.best(unit.action, currentBullies)
      unit.action.movementProfile = MovementProfiles.safelyAttackTarget
  
      if (unit.readyForAttackOrder) {
        Attack.delegate(unit)
      }
      else {
        Kite.delegate(unit)
      }
    }
  }
  
  private def bullies(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.allies
      .filter(neighbor =>
        neighbor.unitClass.isWorker ||
        (
          neighbor.unitClass.isBuilding &&
          (neighbor.wounded || neighbor.unitClass.canAttack))
        )
      .flatMap(neighbor => unit.matchups.targets.filter(_.isBeingViolentTo(neighbor)))
  }
}