package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {
  
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
    lazy val alliesAssisting  = target.matchups.threats.exists(ally =>
      ally != actor
      && ally.unitClass.orderable
      && (ally.topSpeed >= target.topSpeed || ally.topSpeed > actor.topSpeed || ally.inRangeToAttack(target) )
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
  
    lazy val targetBusy       = target.gathering || target.constructing || target.repairing || ! target.canMove
    lazy val targetCatchable  = actor.topSpeed >= target.topSpeed * 1.2 || actor.inRangeToAttack(target) || targetBusy || alliesAssisting || actor.is(Zerg.Scourge)
    lazy val targetReachable  = target.visible || actor.flying || ! target.flying || With.grids.walkableTerrain.get(target.tileIncludingCenter)
    
    val output = targetReachable && (targetCatchable || atOurWorkers)
    
    output
  }
  
}
