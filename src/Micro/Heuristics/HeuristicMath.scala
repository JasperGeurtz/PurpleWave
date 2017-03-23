package Micro.Heuristics

object HeuristicMath {
  
  def unboolify(value:Boolean):Double = if (value) 2.0 else 1.0
  
  val heuristicMaximum = 100000.0
  val heuristicMinimum = 1.0
  
  def normalize(value:Double) = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
}