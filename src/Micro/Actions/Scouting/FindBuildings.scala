package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Physics.Gravity
import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.{Race, UnitCommandType}

object FindBuildings extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.geography.enemyBases.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val tilesToScout = With.geography.enemyBases
      .flatMap(base => {
        val tiles = base.zone.tiles.filter(With.grids.walkable.get)
        if (base.owner.race == Race.Zerg) {
          tiles.filter(tile => With.grids.creep.get(tile) || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0)
        }
        else {
          tiles
        }
      })
  
    if (tilesToScout.isEmpty) return
    
    val pulls = tilesToScout.map(tile => Gravity(
      tile.pixelCenter,
      With.grids.friendlyVision.framesSince(tile)))
    
    val force = pulls.map(_.apply(unit.pixelCenter)).reduce(_ + _)
    
    //TODO: Use actual potential flow so we can avoid obstacles and threats
    val target = unit.pixelCenter.project(unit.pixelCenter.add(force.pixel), 32.0)
    unit.agent.toTravel = Some(target)
    Travel.delegate(unit)
  }
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.filter(builder =>
      builder.unitClass.isWorker &&
      (
        builder.command.exists(_.getUnitCommandType == UnitCommandType.Build) ||
        builder.targetPixel.exists(targetPixel => targetPixel.zone.bases.exists(_.townHallArea.contains(targetPixel.tileIncluding)))
      ))
  }
}
