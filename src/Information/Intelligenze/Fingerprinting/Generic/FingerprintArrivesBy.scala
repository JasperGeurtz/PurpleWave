package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.UnitInfo

class FingerprintArrivesBy(
  unitMatcher : UnitMatcher,
  gameTime    : GameTime,
  quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true
  
  override def investigate: Boolean = {
    val units           = With.units.ever.filter(u => u.isEnemy && unitMatcher.accept(u))
    val targetFrame     = gameTime.frames
    val arrivalTimes    = units.map(u => (u, arrivaltime(u))).toMap
    val arrivingOnTime  = arrivalTimes.count(_._2 < targetFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
  
  protected def arrivaltime(unit: UnitInfo): Int = {
    val home        = With.geography.home.pixelCenter
    val classSpeed  = unit.unitClass.topSpeed
    val travelTime  = Math.min(24 * 60 * 60,
      if (unit.canMove)
        unit.framesToTravelTo(home)
      else if (classSpeed > 0)
        (unit.pixelDistanceTravelling(home) / classSpeed).toInt
      else
        Int.MaxValue)
    
    val completionTime  = PurpleMath.clamp(unit.completionFrame, With.frame, With.frame + unit.unitClass.buildFrames)
    val arrivalTime     = completionTime + travelTime
    arrivalTime
  }
}
