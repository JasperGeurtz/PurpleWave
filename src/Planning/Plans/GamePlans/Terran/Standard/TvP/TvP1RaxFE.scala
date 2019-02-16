package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Situational.RepairBunker
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Terran.{BuildBunkersAtNatural, BuildMissileTurretsAtNatural, PopulateBunkers}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP1RaxFE

class TvP1RaxFE extends GameplanTemplate {

  override val activationCriteria = new Employing(TvP1RaxFE)
  override val completionCriteria = new Latch(new And(
    new BasesAtLeast(2),
    new TechStarted(Terran.SiegeMode),
    new UnitsAtLeast(1, Terran.EngineeringBay)
  ))

   override def placementPlan: Plan = new ProposePlacement {
     override lazy val blueprints: Seq[Blueprint] = Vector(
       new Blueprint(this, building = Some(Terran.Barracks), preferZone = Some(With.geography.ourNatural.zone)),
       new Blueprint(this, building = Some(Terran.SupplyDepot), preferZone = Some(With.geography.ourNatural.zone)))
   }

  override def scoutPlan: Plan = new ScoutOn(Terran.Factory)

  override def attackPlan = new If(
    new EnemyStrategy(With.fingerprints.nexusFirst),
    new ConsiderAttacking)

  override def buildOrderPlan = new Parallel(
    new BuildOrder(
      Get(9,  Terran.SCV),
      Get(Terran.SupplyDepot),
      Get(11, Terran.SCV),
      Get(Terran.Barracks),
      Get(15, Terran.SCV),
      Get(2,  Terran.CommandCenter),
      Get(1,  Terran.Marine),
      Get(2,  Terran.SupplyDepot),
      Get(Terran.Refinery)),
    new Pump(Terran.Marine, 4),
    new FlipIf(
      new Or(
        new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
        new EnemyHasShown(Protoss.Zealot)),
      new Build(
        Get(18, Terran.SCV),
        Get(Terran.Factory)),
      new BuildBunkersAtNatural(1))
  )

  override def buildPlans: Seq[Plan] = Seq(
    new RepairBunker,
    new PopulateBunkers,

    new CapGasAt(250),

    new TvPIdeas.ReactiveEarlyVulture,
    new Pump(Terran.SiegeTankUnsieged),
    new Build(Get(Terran.MachineShop)),

    new If(
      new EnemyDarkTemplarLikely,
      new BuildMissileTurretsAtNatural(1)),

    new Trigger(
      new EnemiesAtLeast(2, Protoss.Dragoon),
      new Build(
        Get(Terran.SiegeMode),
        Get(2, Terran.Factory),
        Get(2, Terran.Refinery),
        Get(2, Terran.MachineShop))),

    new BuildMissileTurretsAtNatural(1),
    new Build(
      Get(2, Terran.Factory),
      Get(2, Terran.Refinery),
      Get(Terran.SiegeMode)),

    new Pump(Terran.Marine, 5)
  )
}