package Information.Geography.Types

import Information.Geography.Pathfinding.ZonePath
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import bwta.Region

import scala.collection.JavaConverters._
import scala.collection.mutable

class Zone(
  val bwtaRegion  : Region,
  val boundary    : TileRectangle,
  val tiles       : mutable.Set[Tile]) {
  
  lazy val  edges           : Array[Edge]       = With.geography.edges.filter(_.zones.contains(this)).toArray
  lazy val  bases           : Array[Base]       = With.geography.bases.filter(_.townHallTile.zone == this).toArray
  lazy val  border          : Set[Tile]         = tiles.filter(tile => Array(tile.up, tile.down, tile.left, tile.right, tile.up.left, tile.up.right, tile.down.left, tile.down.right).exists( ! tiles.contains(_))).toSet
  lazy val  centroid        : Tile              = if (tiles.isEmpty) new Pixel(bwtaRegion.getCenter).tileIncluding else tiles.minBy(_.tileDistanceSquared(new Pixel(bwtaRegion.getCenter).tileIncluding))
  lazy val  area            : Double            = bwtaRegion.getPolygon.getArea
  lazy val  points          : Iterable[Pixel]   = bwtaRegion.getPolygon.getPoints.asScala.map(new Pixel(_)).toVector
  lazy val  island          : Boolean           = ! With.geography.startBases.map(_.zone).exists(otherZone => this != otherZone && With.paths.zonePath(this, otherZone).isDefined)
  lazy val  tilesBuildable  : Array[Tile]       = { With.grids.buildableTerrain.initialize(); tiles.filter(With.grids.buildableTerrain.get).toArray }
  
  lazy val exit: Option[Edge] = {
    if (edges.isEmpty)
      None
    else
      // Uses ground distance, but seems to work okay
      Some(edges.minBy(edge => With.geography.startLocations.map(_.groundPixels(edge.centerPixel)).max))
  }
  
  var owner     : PlayerInfo  = With.neutral
  var contested : Boolean     = false
  var walledIn  : Boolean     = false
  
  def contains(tile: Tile)    : Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel: Pixel)  : Boolean = contains(pixel.tileIncluding)
  
  def pathTo          (to: Zone): Option[ZonePath]  = With.paths.zonePath(this, to)
  def canWalkTo       (to: Zone): Boolean           = pathTo(to).isDefined
  def distancePixels  (to: Zone): Double            = With.paths.zoneDistance(this, to)
}
