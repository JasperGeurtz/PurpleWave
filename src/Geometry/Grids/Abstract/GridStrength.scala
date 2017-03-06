package Geometry.Grids.Abstract

import Geometry.Circle
import Global.Combat.Battle.BattleMetrics
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Caching.Limiter
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

abstract class GridStrength extends GridInt {
  
  val _limitUpdates = new Limiter(4, _update)
  override def update() {
    _limitUpdates.act()
  }
  
  val rangeMargin = 0
  val framesToLookAhead = 36
  
  def _update() {
    reset()
    _getUnits.foreach(unit => {
      val strength = BattleMetrics.evaluate(unit)
      val latencyFrames = With.game.getLatencyFrames
      val tilePosition = unit.position.toTilePosition //position.toTilePosition uses the unit's center rather than its top-left corner
      val rangeFull = unit.range + rangeMargin
      val rangeZero = unit.range + rangeMargin + (unit.utype.topSpeed * (framesToLookAhead + latencyFrames)).toInt
      if (strength > 0) {
        _populate(tilePosition, rangeFull, rangeZero, strength)
      }
    })
  }
  
  def _populate(tile:TilePosition, distanceFull:Int, distanceZero:Int, strength:Int) {
    Circle.points(distanceZero/32).foreach(point => {
      val nearbyTile = tile.add(point)
      val distance = Math.sqrt(32 * 32 * point.lengthSquared)
      val ratio = Math.min(1, Math.max(0, (distanceZero - distance) / (distanceZero - distanceFull)))
      add(nearbyTile, (strength * ratio).toInt)
    })
  }
  
  def _getUnits:Iterable[UnitInfo]
}