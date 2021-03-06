package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtMost, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP2Gate1012DT, PvP2Gate1012Goon}

class PvP2Gate1012GoonOrDT extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP2Gate1012Goon, PvP2Gate1012DT)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),         placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 8.0)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(this, building = Some(Protoss.Pylon),         placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.ShieldBattery)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))

  override def priorityAttackPlan: Plan = new Parallel(
    new If(
      new EnemyStrategy(With.fingerprints.proxyGateway),
      new Attack(Protoss.Zealot, UnitCountExactly(1))),
    new AttackWithDarkTemplar)

  override def attackPlan: Plan = new If(
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)),
      new UnitsAtLeast(3, Protoss.Dragoon, complete = true)),
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true),
        new EnemiesAtMost(0, Protoss.Observer)),
      new Attack,
      new If(
        new Not(
          new And(
            new MiningBasesAtLeast(2),
            new EnemyStrategy(With.fingerprints.fourGateGoon))),
        new PvPIdeas.AttackSafely)))

  override val scoutPlan: Plan = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  class GoDT extends And(
    new Or(
      new Employing(PvP2Gate1012DT),
      new EnemyStrategy(With.fingerprints.fourGateGoon, With.fingerprints.proxyGateway)),
    new Or(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      new Not(new EnemyStrategy(With.fingerprints.robo))))

  class NeedForgeToExpand extends Or(
    new EnemyStrategy(With.fingerprints.dtRush, With.fingerprints.forgeFe), // Forge FE can hide a DT rush
    new And(
      new Not(new EnemyStrategy(With.fingerprints.robo)),
      new EnemyBasesAtMost(1)))

  class Expand extends Parallel(
    new If(new NeedForgeToExpand, new Build(Get(Protoss.Forge))),
    new RequireMiningBases(2))


  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(

    new CapGasAt(300),
    new If(
      new And(
        new UnitsAtMost(2, Protoss.Gateway),
        new Not(new GoDT)),
      new CapGasWorkersAt(2)),

    new If(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate), new BuildOrder(Get(7, Protoss.Zealot))),

    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),

    new If(
      new And(
        new GoDT,
        new UnitsAtLeast(1, Protoss.CyberneticsCore)),
      new EjectScout(Protoss.Probe)),

    new If(
      new EnemyStrategy(With.fingerprints.forgeFe),
      new BuildOrder(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Shuttle),
        Get(Protoss.RoboticsSupportBay))),

    new If(new Not(new GoDT), new Build(Get(Protoss.DragoonRange))),

    new If(
      new Or(
        new UnitsAtLeast(2, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true),
        new And(new SafeAtHome, new UnitsAtLeast(12, UnitMatchWarriors))),
      new Expand),

    new If(
      new GoDT,
      new BuildOrder(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(Protoss.ZealotSpeed),
        Get(2, Protoss.DarkTemplar))),

    new If(
      new And(
        new GoDT,
        new Not(new UpgradeStarted(Protoss.DragoonRange))),
      new Parallel(
        new Pump(Protoss.DarkTemplar, 4),
        new Pump(Protoss.Zealot),
        new Trigger(
          new UnitsAtLeast(2, Protoss.DarkTemplar),
          new Build(Get(Protoss.DragoonRange))))),

    new FlipIf(
      new SafeAtHome,
      new PvPIdeas.TrainArmy,
      new Build(Get(3, Protoss.Gateway))),

    new FlipIf(
      new SafeAtHome,
      new Expand,
      new Build(Get(4, Protoss.Gateway))),

    new If(new NeedForgeToExpand, new BuildCannonsAtNatural(2)),
  )
}
