package Micro.Actions
import Information.Battles.TacticsTypes.Tactics
import Micro.Intent.Intention
import Planning.Yolo
import ProxyBwapi.UnitInfo.UnitInfo

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    Yolo.disabled &&
    intent.unit.canMoveThisFrame &&
    intent.tactics.exists(_.has(Tactics.Movement.Kite))
  }
  
  override def perform(intent: Intention): Boolean = {
    
    //This interpretation of kiting doesn't quite line up with the battle simulator, which may cause unintended behavior.
    if (intent.threats.exists(threat =>
      isAttackingUs(intent, threat) &&
      canReachUsBeforeWeShoot(intent, threat))) {
      if (Flee.consider(intent)) return true
    }
    if (intent.threats.forall(threat => outRangesUs(intent, threat))) {
      if (Flee.consider(intent)) return true
    }
    
    if (intent.unit.canAttackThisFrame) {
      if (Attack.consider(intent)) return true
    }
    
    Move.consider(intent)
  }
  
  def isAttackingUs(intent:Intention, threat:UnitInfo):Boolean = {
    threat.target.orElse(threat.orderTarget).exists(_ == intent.unit) ||
    threat.targetPixel.orElse(threat.orderTargetPixel).exists(pixel =>
      intent.unit.pixelDistanceSquared(pixel) < intent.unit.pixelDistanceSquared(pixel) &&
      intent.unit.pixelDistanceSquared(pixel) <= threat.rangeAgainst(intent.unit) * threat.rangeAgainst(intent.unit)
    )
  }
  
  def canReachUsBeforeWeShoot(intent:Intention, threat:UnitInfo):Boolean = {
    //TODO: Use a better estimate of this
    val framesToLookAhead = 8
    threat.pixelImpactAgainst(framesToLookAhead, intent.unit) <= threat.pixelDistanceFast(intent.unit)
  }
  
  def outRangesUs(intent:Intention, threat:UnitInfo):Boolean = {
    threat.rangeAgainst(intent.unit) >= intent.unit.rangeAgainst(threat)
  }
}