package Micro.Actions.Scouting

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toTravel.isDefined  &&
    unit.agent.canScout            &&
    unit.canAttack
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    FindBuildings.consider(unit)
  }
}
