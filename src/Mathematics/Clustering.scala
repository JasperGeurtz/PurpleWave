package Mathematics

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

object Clustering {
  
  def groupUnits(
    units         : Iterable[UnitInfo],
    radius        : Int,
    limitRegion   : Boolean = false)
      : mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] = {
    group(units, radius, limitRegion, (u) => u.pixelCenter)
  }
  
  def group[T](
    things        : Iterable[T],
    radius        : Double,
    limitRegion   : Boolean = false,
    extractPixel  : (T) => Pixel)
      :mutable.HashMap[T, mutable.HashSet[T]] = {
    
    val neighborsByUnit = mapUnitsToNeighbors(things, radius, extractPixel)
    val unitLeaders = new mutable.HashMap[T, T]
    val groupsByLeader = new mutable.HashMap[T, mutable.HashSet[T]] {
      override def default(key: T):mutable.HashSet[T] = {
        put(key, new mutable.HashSet[T])
        this(key)}}
    
    things.foreach(thing => {
      if ( ! unitLeaders.contains(thing)) {
        groupsByLeader(thing).add(thing)
        groupsByLeader(thing) ++= neighborsByUnit(thing).filter(neighbor =>
          ! limitRegion ||
          (
            With.grids.altitudeBonus.get(extractPixel(thing).tileIncluding) ==
            With.grids.altitudeBonus.get(extractPixel(neighbor).tileIncluding) &&
            extractPixel(thing).zone ==
            extractPixel(neighbor).zone
          ))
        groupsByLeader(thing).foreach(groupMember => unitLeaders.put(groupMember, thing))
      }})
    
    groupsByLeader
  }
  
  private def mapUnitsToNeighbors[T](
    things        : Iterable[T],
    radius        : Double,
    extractPixel  : (T) => Pixel)
      :Map[T, Iterable[T]] = {
    
    val radiusSquared = radius * radius
    
    //Yes, this includes the unit itself
    things.map(thing => (thing, things.filter(extractPixel(_).pixelDistanceSquared(extractPixel(thing)) <= radiusSquared))).toMap
  }
}
