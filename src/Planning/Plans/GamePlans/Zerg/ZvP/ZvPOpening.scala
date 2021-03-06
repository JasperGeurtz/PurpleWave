package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, DefendAgainstWorkerRush, EjectScout}
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendAgainstProxy
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Scouting.{ScoutAt, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{BasesAtLeast, GasForUpgrade, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.{ZvP12Hatch, ZvP9Pool, ZvPOverpool}

class ZvPOpening extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvP12Hatch, ZvPOverpool, ZvP9Pool)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(3))

  override def attackPlan: Plan = new Attack

  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new If(
      new Not(new EnemyStrategy(With.fingerprints.forgeFe)),
      new If(
        new Employing(ZvP12Hatch),
        new ScoutAt(8),
        new ScoutOn(Zerg.SpawningPool))))

  override def emergencyPlans: Seq[Plan] = Seq(
    new DefendAgainstWorkerRush,
    new DefendAgainstProxy
  )

  override def buildOrderPlan = new Parallel(
    new If(
      new Employing(ZvP12Hatch),
        new BuildOrder(
        Get(9, Zerg.Drone),
        Get(2, Zerg.Overlord),
        Get(12, Zerg.Drone))),
    new If(
      new Employing(ZvPOverpool),
        new BuildOrder(
          Get(9, Zerg.Drone),
          Get(2, Zerg.Overlord),
          Get(Zerg.SpawningPool),
          Get(11, Zerg.Drone))),
    new If(
      new Employing(ZvP9Pool),
        new BuildOrder(
          Get(9, Zerg.Drone),
          Get(Zerg.SpawningPool),
          Get(10, Zerg.Drone),
          Get(2, Zerg.Overlord),
          Get(11, Zerg.Drone)))
  )

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new If(
      new UnitsAtMost(0, Zerg.Spire),
      new CapGasAt(150),
      new CapGasAtRatioToMinerals(1.0, 300)),

    new Pump(Zerg.SunkenColony),
    new If(
      new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate),
      new Parallel(
        new Build(Get(Zerg.SpawningPool)),
        new BuildSunkensAtNatural(2),
        new BuildOrder(Get(8, Zerg.Zergling)))),
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new BuildOrder(Get(8, Zerg.Mutalisk))),

    new Trigger(
      new Or(
        new Employing(ZvP12Hatch),
        new UnitsAtLeast(1, Zerg.SpawningPool, complete = true)),
      new Parallel(
        new If(
          new Employing(ZvPOverpool),
          new If(
            new EnemyStrategy(With.fingerprints.forgeFe),
            new BuildOrder(Get(14, Zerg.Drone)),
            new BuildOrder(Get(6, Zerg.Zergling)))),
        new If(
          new Employing(ZvP9Pool),
          new If(
            new And(new EnemyStrategy(With.fingerprints.forgeFe), new UnitsAtLeast(1, Protoss.PhotonCannon)),
            new BuildOrder(Get(14, Zerg.Drone)),
            new BuildOrder(Get(6, Zerg.Zergling)))),

        new RequireMiningBases(2),

        // If -- vs. Nexus-first
        new If(
          new EnemyStrategy(With.fingerprints.nexusFirst),
          new If(
            new Employing(ZvPOverpool, ZvP9Pool),
            new Parallel(
              new Pump(Zerg.Zergling),
              new If(
                new GasForUpgrade(Zerg.ZerglingSpeed),
                new CapGasWorkersAt(0)),
              new Build(
                Get(Zerg.Extractor),
                Get(Zerg.ZerglingSpeed)),
              new RequireMiningBases(3)),
            new Parallel(
              new Pump(Zerg.Drone, 13),
              new RequireMiningBases(3),
              new Pump(Zerg.Drone))),

        // Else -- vs. Forge FE
        new If(
          new EnemyStrategy(With.fingerprints.forgeFe),
          new Parallel(
            new Pump(Zerg.Drone, 13),
            new RequireMiningBases(3),
            new Pump(Zerg.Drone),
            new Build(Get(Zerg.SpawningPool))),

        // Else -- vs. Gateway FE
        new If(
          new EnemyStrategy(With.fingerprints.gatewayFe),
          new Parallel(
            new Build(Get(Zerg.SpawningPool)),
            new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(UnitMatchWarriors, 4.5))),
            new Pump(Zerg.Drone, 13),
            new RequireMiningBases(3),
            new Pump(Zerg.Drone)),

        // Else -- vs. 1 Gate Core
        new If(
          new EnemyStrategy(With.fingerprints.oneGateCore),
          new Parallel(
            new BuildOrder(
              Get(Zerg.SpawningPool),
              Get(14, Zerg.Drone),
              Get(8, Zerg.Zergling)),
            new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(UnitMatchWarriors, 4.5))),
            new Pump(Zerg.Drone),
            new BuildOrder(
              Get(Zerg.Extractor),
              Get(Zerg.Lair),
              Get(Zerg.ZerglingSpeed)),
            new RequireMiningBases(3)),

        // Else -- vs. 2-Gate / Unknown
        new Parallel(
          new BuildOrder(Get(Zerg.SpawningPool)),
          new PumpRatio(Zerg.Drone, 9, 15, Seq(Flat(9), Friendly(Zerg.SunkenColony, 2))),
          new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(UnitMatchWarriors, 4.5))),
          new Pump(Zerg.Drone, 13),
          new If(
            new EnemyStrategy(With.fingerprints.proxyGateway),
            new Parallel(
              new Build(
                Get(Zerg.Extractor),
                Get(Zerg.ZerglingSpeed)),
              new Pump(Zerg.Zergling))),
          new Pump(Zerg.Drone, 18),
          new Pump(Zerg.Mutalisk),
          new Build(
            Get(Zerg.Extractor),
            Get(Zerg.Lair),
            Get(Zerg.Spire)),
          new If(
            new UnitsAtLeast(1, Zerg.Spire),
            new Build(Get(2, Zerg.Extractor))),
          new RequireMiningBases(3),
        ))))))
    )
  )
}
