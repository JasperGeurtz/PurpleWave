package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgame4Gate2Archon

class PvZBisu extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgame4Gate2Archon)
  override val completionCriteria = new Latch(new Or(new MiningBasesAtLeast(3), new TechComplete(Protoss.PsionicStorm)))
  override def defaultArchonPlan: Plan = new MeldArchons(49)
  override def defaultAttackPlan: Plan = new Trigger(new UnitsAtLeast(2, Protoss.Archon), new Attack, new PvZIdeas.ConditionalAttack)

  override def defaultWorkerPlan: Plan = NoPlan()
  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers, new PvZIdeas.ReactToMutalisks)
  
  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new Pump(Protoss.DarkTemplar, 2),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Get(Protoss.PsionicStorm))),
    new Pump(Protoss.HighTemplar),
    new If(
      new And(
        new EnemiesAtLeast(3, Zerg.Mutalisk),
        new UnitsAtMost(0, Protoss.TemplarArchives)),
      new Parallel(
        new Pump(Protoss.Dragoon, 8),
        new UpgradeContinuously(Protoss.DragoonRange))),
    new Pump(Protoss.Dragoon, 1),
    new Pump(Protoss.Zealot),
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(2, Protoss.Assimilator),
      Get(1, Protoss.CitadelOfAdun)),
    new PumpWorkers(),
    new Build(
      Get(Protoss.GroundDamage),
      Get(1, Protoss.TemplarArchives),
      Get(4, Protoss.Gateway),
      Get(Protoss.ZealotSpeed),
      Get(6, Protoss.Gateway),
      Get(Protoss.PsionicStorm),
      Get(8, Protoss.Gateway)),

    new RequireMiningBases(3)
  )
}
