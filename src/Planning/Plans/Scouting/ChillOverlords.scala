package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Plan
import Planning.Plans.Predicates.Milestones.EnemyHasShownCloakedThreat
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class ChillOverlords extends Plan {
  
  val overlords = new LockUnits
  overlords.unitMatcher.set(Zerg.Overlord)
  overlords.unitCounter.set(UnitCountEverything)
  
  val cloakedThreat = new EnemyHasShownCloakedThreat
  override def onUpdate() {
    if (With.self.hasUpgrade(Zerg.OverlordSpeed)) {
      return
    }
    if (cloakedThreat.isComplete) {
      return
    }
    overlords.acquire(this)
    overlords.units.foreach(chillOut(_, overlords.units.size))
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int) {
    val base = ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(overlord.pixelDistanceSquared)
    val tile = base.map(b => PurpleMath.sample(Circle.points(Math.sqrt(count).toInt).map(b.tileIncluding.add))).getOrElse(With.geography.home)
    val intent = new Intention
    intent.toTravel = Some(tile.pixelCenter)
    intent.canFlee = true
    overlord.agent.intend(this, intent)
  }
}
