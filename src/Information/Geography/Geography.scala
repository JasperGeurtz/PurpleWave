package Information.Geography

import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Points.{SpecificPoints, Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.collection.JavaConverters._
import scala.collection.mutable

class Geography {
  lazy val mapArea            : TileRectangle         = TileRectangle(Tile(0, 0), Tile(With.mapTileWidth, With.mapTileHeight))
  lazy val allTiles           : Array[Tile]           = mapArea.tiles.indices.map(new Tile(_)).toArray
  lazy val startBases         : Vector[Base]          = bases.filter(_.isStartLocation)
  lazy val startLocations     : Vector[Tile]          = With.game.getStartLocations.asScala.map(new Tile(_)).toVector
  lazy val zones              : Vector[Zone]          = ZoneBuilder.zones.toVector
  lazy val edges              : Vector[Edge]          = ZoneBuilder.edges.toVector
  lazy val bases              : Vector[Base]          = ZoneBuilder.bases.toVector
  lazy val ourMain            : Base                  = With.geography.ourBases.find(_.isStartLocation).getOrElse(With.geography.bases.minBy(_.heart.tileDistanceFast(With.self.startTile)))
  lazy val rushDistances      : Vector[Double]        = startLocations.flatMap(s1 => startLocations.filterNot(_ == s1).map(s2 => s1.groundPixels(s2))).toSet.toVector
  def ourNatural              : Base                  = ourNaturalCache()
  def ourZones                : Vector[Zone]          = ourZonesCache()
  def ourBases                : Vector[Base]          = ourBasesCache()
  def ourSettlements          : Vector[Base]          = ourSettlementsCache()
  def ourBasesAndSettlements  : Vector[Base]          = (ourBases ++ ourSettlements).distinct
  def ourTownHalls            : Vector[UnitInfo]      = ourTownHallsCache()
  def ourHarvestingAreas      : Vector[TileRectangle] = ourHarvestingAreasCache()
  def ourBorder               : Vector[Edge]          = ourBorderCache()
  def enemyZones              : Vector[Zone]          = enemyZonesCache()
  def enemyBases              : Vector[Base]          = enemyBasesCache()
  def neutralBases            : Vector[Base]          = With.geography.bases.filter(_.owner.isNeutral)
  
  private val ourZonesCache           = new Cache(() => zones.filter(_.owner.isUs))
  private val ourBasesCache           = new Cache(() => bases.filter(_.owner.isUs))
  private val ourSettlementsCache     = new Cache(() => getSettlements)
  private val enemyZonesCache         = new Cache(() => zones.filter(_.owner.isEnemy))
  private val enemyBasesCache         = new Cache(() => bases.filter(_.owner.isEnemy))
  private val ourTownHallsCache       = new Cache(() => ourBases.flatMap(_.townHall))
  private val ourHarvestingAreasCache = new Cache(() => ourBases.map(_.harvestingArea))
  private val ourBorderCache          = new Cache(() => ourZones.flatMap(_.edges).filter(_.zones.exists( ! _.owner.isFriendly)))
  private val ourNaturalCache = new Cache(() =>
    (if (ourMain.owner.isUs) ourMain.natural else None)
      .getOrElse(bases.find(_.isNaturalOf.exists(_.owner.isUs))
      .getOrElse(bases.minBy(_.townHallTile.groundPixels(ourMain.townHallTile)))))

  
  def zoneByTile(tile: Tile): Zone = if (tile.valid) zoneByTileCacheValid(tile.i) else zoneByTileCacheInvalid(tile)
  def baseByTile(tile: Tile): Option[Base] = if (tile.valid) baseByTileCacheValid(tile.i) else getBaseForTile(tile)

  private lazy val zoneByTileCacheValid = allTiles.map(tile =>
    zones.find(_.tiles.contains(tile)).getOrElse(getZoneForTile(tile)))
  private lazy val baseByTileCacheValid = allTiles.map(getBaseForTile)

  private val zoneByTileCacheInvalid = new mutable.HashMap[Tile, Zone] {
    override def default(key: Tile): Zone = {
      val zone: Zone = getZoneForTile(key)
      put(key, zone)
      zone
    }
  }
  private def getZoneForTile(tile: Tile): Zone =
    ByOption
      .maxBy(
        Spiral
          .points(8)
          .map(point => {
            val neighbor = tile.add(point)
            if (neighbor.valid) zones.find(_.tiles.contains(neighbor)) else None
          })
          .filter(_.isDefined)
          .map(z => z.get)
          .groupBy(x => x))(_._2.size)
      .map(_._1)
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
  private def getBaseForTile(tile: Tile): Option[Base] = ByOption.minBy(tile.zone.bases)(_.heart.tileDistanceSquared(tile))


  private def getSettlements: Vector[Base] = (Vector.empty
  ++ With.geography.bases.filter(_.units.exists(u => u.isOurs && u.unitClass.isBuilding))
  ++ Vector(With.geography.ourNatural).filter(x =>
      With.strategy.isInverted
      && With.units.ours.exists(u => u.complete && u.unitClass.ranged)
      && (With.units.enemy.exists(_.unitClass.ranged) || With.battles.global.globalSafeToAttack))
  ++ With.units.ours
    .view
    .filter(u => u.agent.toBuild.exists(_.isTownHall))
    .flatMap(u => u.agent.toBuildTile.map(tile => tile.zone.bases.find(base => base.townHallTile == tile)))
    .flatten
    .filterNot(_.owner.isUs)
    .toVector
  ).distinct
  
  var home: Tile = SpecificPoints.tileMiddle
  
  var naturalsSearched: Boolean = false
  
  def update() {
    ZoneUpdater.update()
    zones.foreach(_.distanceGrid.initialize())
    zones.foreach(_.edges.foreach(_.distanceGrid.initialize()))
    bases.filter(base => With.game.isVisible(base.townHallArea.midpoint.bwapi)).foreach(base => base.lastScoutedFrame = With.frame)
  }
}
