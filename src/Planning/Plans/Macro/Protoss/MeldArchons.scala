package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitPreferences.UnitPreferLowEnergy
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons(maxEnergy: Int = 250) extends Plan {
  
  protected def maximumTemplar: Int = 100
  protected def minimumArchons: Int = 0
  
  val templar = new LockUnits
  templar.unitMatcher.set(Protoss.HighTemplar)
  templar.unitPreference.set(UnitPreferLowEnergy)
  
  override def onUpdate() {
    // Fast check for performance
    val proceed = With.self.isProtoss && With.units.existsOurs(Protoss.HighTemplar)
    if ( ! proceed) return

    val templarNow    = With.units.countOurs(Protoss.HighTemplar)
    val templarLow    = With.units.countOursP(u => u.is(Protoss.HighTemplar) && u.energy < maxEnergy)
    val templarExcess = templarNow - maximumTemplar
    val archonsNow    = With.units.countOurs(Protoss.Archon)
    val archonsToAdd  = Vector(0, minimumArchons - archonsNow, templarExcess / 2).max
    val templarToMeld = Math.max(templarLow, 2 * archonsToAdd)
    templar.unitCounter.set(new UnitCountBetween(0, templarToMeld))
    With.blackboard.keepingHighTemplar.set(templarExcess < templarNow)
    
    templar.release()
    templar.acquire(this)
    templar.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
