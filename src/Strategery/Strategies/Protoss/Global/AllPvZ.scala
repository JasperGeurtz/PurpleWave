package Strategery.Strategies.Protoss.Global

import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvZ extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossVsZergChoices.openers
  )
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}