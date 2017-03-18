package Macro.Scheduling.Optimization

import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Macro.Buildables.{Buildable, BuildableTech, BuildableUnit, BuildableUpgrade}
import Macro.Scheduling.BuildEvent
import Startup.With

object ScheduleSimulationEventAnticipator {
  
  def anticipate:Iterable[BuildEvent] = {
    With.units.ours.toList.flatten(unit => {
      List(
        getUnitCompletion(unit, unit.framesBeforeBecomingComplete),
        getTechCompletion(unit),
        getUpgradeCompletion(unit)
      ).flatten
    })
  }
  
  def getUnitCompletion(unit:FriendlyUnitInfo, timeLeft:Int):
      Iterable[BuildEvent] = {
    if (timeLeft <= 0) return List.empty
    List(buildEvent(new BuildableUnit(unit.unitClass), timeLeft))
  }
  
  def getTechCompletion(unit:FriendlyUnitInfo): Iterable[BuildEvent] = {
    val timeLeft = unit.framesBeforeTechComplete
    if (timeLeft <= 0) return List.empty
    List(buildEvent(new BuildableTech(unit.teching), timeLeft))
  }
  
  def getUpgradeCompletion(unit:FriendlyUnitInfo): Iterable[BuildEvent] = {
    val timeLeft = unit.framesBeforeUpgradeComplete
    if (timeLeft <= 0) return List.empty
    val upgrade = unit.upgrading
    val level = 1 + With.self.getUpgradeLevel(upgrade.baseType)
    List(buildEvent(new BuildableUpgrade(upgrade, level), timeLeft))
  }
  
  def buildEvent(buildable:Buildable, framesLeft:Int):BuildEvent =
    new BuildEvent(buildable, -1, With.frame + framesLeft)
}
