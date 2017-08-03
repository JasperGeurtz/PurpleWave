package Strategery.Strategies.Protoss.Global

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.ProxyDarkTemplarRush
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyDarkTemplar extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new ProxyDarkTemplarRush) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
