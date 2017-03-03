package Geometry

import bwapi.{Position, TilePosition}
import Utilities.Enrichment.EnrichPosition._

class TileRectangle(
 val startInclusive:TilePosition,
 val endExclusive:TilePosition) {
  
  if (endExclusive.getX < startInclusive.getX || endExclusive.getY < startInclusive.getY) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def contains(x:Int, y:Int):Boolean = {
    x >= startInclusive.getX &&
    y >= startInclusive.getY &&
    x < endExclusive.getX &&
    y < endExclusive.getY
  }
  
  def contains(point:TilePosition):Boolean = {
    contains(point.getX, point.getY)
  }
  
  def intersects(otherRectangle: TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive) ||
    contains(otherRectangle.endExclusive) ||
    contains(otherRectangle.startInclusive.getX, otherRectangle.endExclusive.getY - 1) ||
    contains(otherRectangle.endExclusive.getX - 1, otherRectangle.startInclusive.getY)
  }
  
  def startPosition:Position = {
    startInclusive.toPosition
  }
  
  def endPosition:Position = {
    endExclusive.toPosition.subtract(1, 1)
  }
  
  def toWalkRectangle:WalkRectangle = {
    new WalkRectangle(
      startInclusive.toWalkPosition,
      endExclusive.toWalkPosition)
  }
}
