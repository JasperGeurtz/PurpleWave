package Strategery.Strategies.Options.Protoss.PvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object Early2Gate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      Midgame5GateDragoons,
      MidgameCorsairReaver,
      MidgameCorsairSpeedlot))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Zerg)
}