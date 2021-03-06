package Strategery

import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.TvR1Rax
import Strategery.Strategies.Terran.TvZ.TvZProxy8Fact
import Strategery.Strategies.Terran._
import Strategery.Strategies.Zerg._

class EmptyPlaybook {
  val none: Seq[Strategy] = Seq.empty
  lazy val forced   : Seq[Strategy] = none
  lazy val disabled : Seq[Strategy] = none
  val strategyOrder: Seq[Strategy] = Vector(
  )
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
  def enemyName: String = With.enemy.name
  def respectOpponent: Boolean = true
  def respectMap: Boolean = true
  def respectHistory: Boolean = true
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,

    CarriersWithNoDefense,

    TvEWorkerRushLiftoff,
    TvR1Rax,
    TvZProxy8Fact,
    TvZ2RaxNuke,

    PvROpenZZCore,
    PvE3BaseIslandCarrier,
    PvT1GateRobo,
    PvTProxyDarkTemplar,
    PvT25BaseCarrier, // Experimenting with this so we can delete it
    PvZLateGameCarrier, // Needs island tech
    PvZMidgame4Gate2Archon,
    PvZMidgameNeoNeoBisu,

    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    ZvZ5PoolSunkens,

    DarkArchonsWithNoDefense // Temporary for AIIDE testing
  )
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
}

class AIIDEPlaybook extends PurpleWavePlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionTournament
}

class TestingPlaybook extends PurpleWavePlaybook {
  override lazy val forced: Seq[Strategy] = Seq(ZvP12Hatch, ZvPReactiveBust)
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
  override def respectOpponent: Boolean = false
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

class HumanPlaybook extends PurpleWavePlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionSequence(Seq(
    Seq(PvPRobo),
    Seq(PvP2GateDTExpand),
    Seq(PvP3GateGoon),
    Seq(PvP2Gate1012Goon)
  ))
}

object Playbook extends TestingPlaybook {}
