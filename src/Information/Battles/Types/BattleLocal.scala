package Information.Battles.Types

import Information.Battles.Prediction.Prediction
import Information.Battles.Prediction.Simulation.Simulation
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationSimulationAttack   : Prediction  = estimateSimulation(this, weAttack = true,   weSnipe = false)
  lazy val estimationSimulationSnipe    : Prediction  = estimateSimulation(this, weAttack = true,   weSnipe = true)
  lazy val estimationSimulationRetreat  : Prediction  = estimateSimulation(this, weAttack = false,  weSnipe = false)
  
  lazy val hysteresis     : Double  = ByOption.mean(us.units.filter(_.canMove).map(hysteresisRatio)).getOrElse(0.0)
  lazy val distanceUs     : Double  = focus.pixelDistanceFast(With.geography.home.pixelCenter)
  lazy val distanceEnemy  : Double  = focus.pixelDistanceFast(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  lazy val distanceRatio  : Double  = distanceEnemy / (distanceUs + distanceEnemy)
  lazy val urgency        : Double  = PurpleMath.clamp(0.5 * (distanceRatio - 0.5), 0.0, 0.5)
  lazy val valueUs        : Double  = us.units.map(_.subjectiveValue).sum
  lazy val valueEnemy     : Double  = enemy.units.map(_.subjectiveValue).sum
  lazy val attackGains    : Double  = estimationSimulationAttack.costToEnemy
  lazy val attackLosses   : Double  = estimationSimulationAttack.costToUs
  lazy val snipeGains     : Double  = estimationSimulationSnipe.costToEnemy
  lazy val snipeLosses    : Double  = estimationSimulationSnipe.costToUs
  lazy val retreatGains   : Double  = estimationSimulationRetreat.costToEnemy
  lazy val retreatLosses  : Double  = estimationSimulationRetreat.costToUs
  lazy val ratioAttack    : Double  = PurpleMath.nanToInfinity(attackGains / (attackGains + attackLosses))
  lazy val ratioSnipe     : Double  = PurpleMath.nanToInfinity(snipeGains / (snipeGains + snipeLosses))
  lazy val ratioTarget    : Double  = Math.min(2.0, PurpleMath.nanToZero((With.battles.global.valueRatioTarget + hysteresis - urgency) / With.blackboard.aggressionRatio))
  lazy val netEngageValue : Double  = With.blackboard.aggressionRatio * attackGains + retreatLosses - retreatGains - attackLosses
  lazy val shouldFight    : Boolean = ratioAttack > ratioTarget || ratioSnipe > ratioTarget
  
  private def estimateSimulation(battle: BattleLocal, weAttack: Boolean, weSnipe: Boolean): Prediction = {
    val simulation = new Simulation(battle, weAttack, weSnipe)
    simulation.run()
    simulation.estimation
  }
  
  private def hysteresisRatio(unit: UnitInfo): Double = {
    if (unit.friendly.isEmpty) return 0.0
    val agent     = unit.friendly.get.agent
    val patience  = agent.combatHysteresisFrames.toDouble / With.configuration.battleHysteresisFrames
    val sign      = if (agent.shouldEngage) -1.0 else 1.0
    val output    = patience * sign * With.configuration.battleHysteresisRatio * (if(unit.unitClass.melee) 2.0 else 1.0)
    output
  }
}
