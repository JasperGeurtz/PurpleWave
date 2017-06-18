package Mathematics.Shapes

import Mathematics.Points.Point

object Square {
  
  def points(width:Int):Iterable[Point] = Rectangle.pointsFromCenter(width, width)
  
  def pointsDownAndRight(count:Int):Iterable[Point] =
    (0 until count).flatten(dy =>
      (0 until count).map(dx =>
        Point(dx, dy)))
}
