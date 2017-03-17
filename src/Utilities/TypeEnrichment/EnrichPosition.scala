package Utilities.TypeEnrichment

import Geometry.{Point, Positions, TileRectangle}
import Startup.With
import bwapi.{Position, TilePosition, WalkPosition}

case object EnrichPosition {
  implicit class EnrichedPositionCollection(positions:Iterable[Position]) {
    def minBound:Position = {
      if (positions.isEmpty) return Positions.middle
      new Position(
        positions.view.map(_.getX).min,
        positions.view.map(_.getY).min)}
    def maxBound:Position = {
      if (positions.isEmpty) return Positions.middle
      new Position(
        positions.view.map(_.getX).max,
        positions.view.map(_.getY).max)}
    def centroid:Position = {
      if (positions.isEmpty) return Positions.middle
      new Position(
        positions.view.map(_.getX).sum / positions.size,
        positions.view.map(_.getY).sum / positions.size)
    }
  }
  
  implicit class EnrichedRectangleCollection(rectangles:Iterable[TileRectangle]) {
    def boundary: TileRectangle =
      new TileRectangle(
        new TilePosition(
          rectangles.map(_.startInclusive.getX).min,
          rectangles.map(_.startInclusive.getY).min),
        new TilePosition(
          rectangles.map(_.endExclusive.getX).max,
          rectangles.map(_.endExclusive.getY).max))
  }
  
  implicit class EnrichedTilePositionCollection(positions:Iterable[TilePosition]) {
    def minBound:TilePosition = {
      if (positions.isEmpty) return Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).min,
        positions.view.map(_.getY).min)}
    def maxBound:TilePosition = {
      if (positions.isEmpty) return Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).max,
        positions.view.map(_.getY).max)}
    def centroid:TilePosition = {
      if (positions.isEmpty) return Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).sum / positions.size,
        positions.view.map(_.getY).sum / positions.size)
    }
  }
  
  implicit class EnrichedPosition(position:Position) {
    //Checking position validity is a frequent operation,
    //but going through BWAPI via BWMirror has a lot of overhead
    def valid:Boolean = {
      position.getX >= 0 &&
      position.getY >= 0 &&
      position.getX < With.mapWidth * 32 &&
      position.getY < With.mapHeight * 32
    }
    def toWalkPosition:WalkPosition = {
      new WalkPosition(position.getX / 4, position.getY / 4)
    }
    def add(dx:Int, dy:Int):Position = {
      new Position(position.getX + dx, position.getY + dy)
    }
    def add (point:Point):Position = {
      add(point.x, point.y)
    }
    def add(otherPosition:Position):Position = {
      add(otherPosition.getX, otherPosition.getY)
    }
    def subtract(dx:Int, dy:Int):Position = {
      add(-dx, -dy)
    }
    def subtract(otherPosition:Position):Position = {
      subtract(otherPosition.getX, otherPosition.getY)
    }
    def multiply(scale:Int):Position = {
      new Position(scale * position.getX, scale * position.getY)
    }
    def divide(scale:Int):Position = {
      new Position(position.getX / scale, position.getY / scale)
    }
    def midpoint(otherPosition:Position):Position = {
      add(otherPosition).divide(2)
    }
    def distancePixels(otherPosition:Position):Double = {
      Math.sqrt(distancePixelsSquared(otherPosition))
    }
    def distancePixelsSquared(otherPosition:Position):Int = {
      val dx = position.getX - otherPosition.getX
      val dy = position.getY - otherPosition.getY
      dx * dx + dy * dy
    }
    def tileIncluding:TilePosition = {
      position.toTilePosition
    }
    def tileNearest:TilePosition = {
      position.add(16, 16).toTilePosition
    }
  }
  
  implicit class EnrichedWalkPosition(position:WalkPosition) {
    def add(dx:Int, dy:Int):WalkPosition = {
      new WalkPosition(position.getX + dx, position.getY + dy)
    }
    def add(point:Point):WalkPosition = {
      add(point.x, point.y)
    }
    def add(otherPosition:WalkPosition):WalkPosition = {
      add(otherPosition.getX, otherPosition.getY)
    }
    def subtract(dx:Int, dy:Int):WalkPosition = {
      add(-dx, -dy)
    }
    def subtract(otherPosition:WalkPosition):WalkPosition = {
      subtract(otherPosition.getX, otherPosition.getY)
    }
    def multiply(scale:Int):WalkPosition = {
      new WalkPosition(scale * position.getX, scale * position.getY)
    }
    def divide(scale:Int):WalkPosition = {
      new WalkPosition(position.getX / scale, position.getY / scale)
    }
    def midpoint(otherPosition:WalkPosition):WalkPosition = {
      add(otherPosition).divide(2)
    }
    def distanceWalk(otherPosition:WalkPosition):Double = {
      Math.sqrt(distanceWalkSquared(otherPosition))
    }
    def distanceWalkSquared(otherPosition:WalkPosition):Int = {
      val dx = position.getX - otherPosition.getX
      val dy = position.getY - otherPosition.getY
      dx * dx + dy * dy
    }
  }
  
  implicit class EnrichedTilePosition(position:TilePosition) {
    //Checking position validity is a frequent operation,
    //but going through BWAPI via BWMirror has a lot of overhead
    def valid:Boolean = {
      position.getX >= 0 &&
      position.getY >= 0 &&
      position.getX < With.mapWidth &&
      position.getY < With.mapHeight
    }
    def add(dx:Int, dy:Int):TilePosition = {
      new TilePosition(position.getX + dx, position.getY + dy)
    }
    def add (point:Point):TilePosition = {
      add(point.x, point.y)
    }
    def add(otherPosition:TilePosition):TilePosition = {
      add(otherPosition.getX, otherPosition.getY)
    }
    def subtract(dx:Int, dy:Int):TilePosition = {
      add(-dx, -dy)
    }
    def subtract(otherPosition:TilePosition):TilePosition = {
      subtract(otherPosition.getX, otherPosition.getY)
    }
    def multiply(scale:Int):TilePosition = {
      new TilePosition(scale * position.getX, scale * position.getY)
    }
    def divide(scale:Int):TilePosition = {
      new TilePosition(position.getX / scale, position.getY / scale)
    }
    def midpoint(otherPosition:TilePosition):TilePosition = {
      add(otherPosition).divide(2)
    }
    def distanceTile(otherPosition:TilePosition):Double = {
      Math.sqrt(distanceTileSquared(otherPosition))
    }
    def distanceTileSquared(otherPosition:TilePosition):Int = {
      val dx = position.getX - otherPosition.getX
      val dy = position.getY - otherPosition.getY
      dx * dx + dy * dy
    }
    def topLeftPixel:Position = {
      position.toPosition
    }
    def bottomRightPixel:Position = {
      position.toPosition.add(31, 31)
    }
    def pixelCenter:Position = {
      position.toPosition.add(16, 16)
    }
    def toWalkPosition:WalkPosition = {
      new WalkPosition(position.getX * 4, position.getY * 4)
    }
  }
}