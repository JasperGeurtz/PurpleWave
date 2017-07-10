package Strategery.Strategies.Options.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1015GateGoon extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}