package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.ProtectZone
import Planning.Plan
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends Plan {
  
  val recruit: Conscript = new Conscript
  val goal: ProtectZone = new ProtectZone(zone)
  
  override def getChildren: Iterable[Plan] = Array(recruit)
  
  var enemies: Iterable[ForeignUnitInfo] = Iterable.empty
  
  override def onUpdate() {
  
    val ourBase = zone.bases.find(base => base.owner.isUs)
  
    recruit.squad.goal  = goal
    recruit.mustFight   = zone.bases.exists(_.owner.isUs)
    recruit.overkill    = if (recruit.mustFight) 1.5 else 2.0
    recruit.enemies     = enemies
    recruit.update()
  }
}