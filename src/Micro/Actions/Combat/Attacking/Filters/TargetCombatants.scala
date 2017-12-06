package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val fighting         = actor.teammates.exists(_.matchups.threatsInRange.nonEmpty)
    lazy val targetAssisting  = target.unitClass.helpsInCombat && ! target.unitClass.attacks
    lazy val targetAggressing = target.hasBeenViolentInLastTwoSeconds
    lazy val targetInRange    = actor.inRangeToAttackFast(target)
    lazy val targetCatchable  = actor.matchups.alliesIncludingSelf.exists(_.topSpeed >= target.topSpeed)
    
    val output = (
      ( ! fighting || targetAssisting || targetAggressing)
      &&
      (targetInRange || targetCatchable)
    )
    
    output
  }
  
}
