package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobileDetectors extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.isDetector && unit.canMove
}
