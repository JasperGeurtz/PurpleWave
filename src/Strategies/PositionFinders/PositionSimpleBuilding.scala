package Strategies.PositionFinders

import Caching.Cache
import Startup.With
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]] { duration = 24 * 2; override def recalculate = _recalculate }
  override def find(): Option[TilePosition] = _cache.get
  
  def _recalculate: Option[TilePosition] = {
    val position = With.map.ourBaseHalls.head.getTilePosition
    val output = With.architect.placeBuilding(
      buildingType,
      position,
      margin = 1,
      searchRadius = 50)
    
    if (output == None) {
      With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ position.toString)
    }
    
    output
  }
}