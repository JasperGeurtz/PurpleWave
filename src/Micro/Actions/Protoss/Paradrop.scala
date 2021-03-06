package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.Manners
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Traverse
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.isAny(Protoss.Reaver, Protoss.HighTemplar)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val reaverCanFight  = unit.scarabCount > 0 && (unit.agent.shouldEngage || unit.matchups.threats.forall(_.pixelRangeAgainst(unit) <= unit.effectiveRangePixels))
    val templarCanFight = unit.energy >= 75
    val readyToDrop     = reaverCanFight || templarCanFight

    // If we're able to fight, pick a target
    if (readyToDrop) {
      if (unit.is(Protoss.Reaver)) {
        Target.consider(unit)
      }
    }

    // If we can drop out and attack now, do so
    if (unit.agent.toAttack.exists(unit.inRangeToAttack)) {
      val shouldDrop = unit.matchups.framesOfSafety > unit.cooldownLeft || (unit.agent.shouldEngage && unit.matchups.threatsInRange.forall(u => u.pixelRangeAgainst(unit) + 16 >= u.effectiveRangePixels))
      if (shouldDrop) {
        Attack.delegate(unit)
        return
      }
    }
    val target = unit.agent.toAttack

    def eligibleTeammate = (unit: UnitInfo) => ! unit.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.HighTemplar)
    lazy val squadmates = unit.squad.map(_.units.toSeq.filter(eligibleTeammate)).getOrElse(Seq.empty)
    lazy val squadmatesEngaged = squadmates.filter(_.matchups.enemies.nonEmpty)
    lazy val allies = unit.matchups.allies.filter(eligibleTeammate)
    lazy val alliesEngaged = allies.filter(_.matchups.enemies.nonEmpty)

    val destinationAir =
      unit.agent.toAttack.map(_.pixelCenter)
        .orElse(if (squadmates.size > 3) Some(PurpleMath.centroid(squadmates.map(_.pixelCenter))) else None)
        .orElse(if (allies.size > 3) Some(PurpleMath.centroid(allies.map(_.pixelCenter))) else None)
        .getOrElse(if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin)
    val destination = destinationAir.nearestWalkableTerrain
    unit.agent.toTravel = Some(destination.pixelCenter)

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    def targetDistance: Float = (unit.effectiveRangePixels + (if (unit.unitClass != Protoss.HighTemplar) unit.topSpeed * unit.cooldownLeft else 0)).toFloat / 32f
    profile.end                 = Some(destination)
    profile.endDistanceMaximum  =
      if (target.isDefined && unit.pixelDistanceCenter(target.get.pixelCenter) > targetDistance)
        targetDistance
      else 0
    profile.maximumLength       = Some(30)
    profile.canCrossUnwalkable  = false
    profile.allowGroundDist     = false
    profile.costOccupancy       = 0.5f
    profile.costThreat          = 3
    profile.costRepulsion       = if (target.isDefined) 1f else 1.5f
    profile.repulsors           = Avoid.pathfindingRepulsion(unit)
    profile.unit                = Some(unit)
    val path = profile.find
    if (path.pathExists) {

      unit.agent.toTravel = Some(path.end.pixelCenter)
      new Traverse(path).delegate(unit)
    } else {
      Manners.debugChat(f"Failed to path $unit to $destination")
      Manners.debugChat(f"Targeting $target")
    }
  }
}
