package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Trigger, _}
import Planning.Plans.GamePlans.Protoss.Situational.BuildHuggingNexus
import Planning.Plans.Macro.Automatic.{PumpWorkers, _}
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.MeldDarkArchons
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Economy.{GasAtLeast, GasAtMost, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeToMoveOut}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT1015DT, PvT1015Expand, PvT32Nexus, PvTStove}

object PvTIdeas {

  class PriorityAttacks extends Parallel(
    new If(new Or(new EnemyUnitsNone(Protoss.Observer), new EnemyBasesAtLeast(3)), new Attack(Protoss.DarkTemplar)),
    new Attack(Protoss.Scout),
    new Trigger(new UnitsAtLeast(4, Protoss.Carrier, complete = true), initialAfter = new Attack(Protoss.Carrier)))

  class PvTAttack extends Trigger(
    new UnitsAtLeast(4, Protoss.Carrier, complete = true),
    new Attack,
    new Attack(UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers), UnitMatchNot(Protoss.Carrier))))

  class AttackSafely extends If(
    new Or(
      new Latch(new UnitsAtLeast(8, UnitMatchWarriors)),
      new Not(new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113))),
    new If(
      new Or(
        new Employing(PvT32Nexus, PvT1015Expand, PvT1015DT, PvTStove),
        new MiningBasesAtLeast(3),
        new EnemyBasesAtLeast(2),
        new EnemyStrategy(With.fingerprints.bio),
        new Not(new EnemyHasShown(Terran.Vulture)),
        new UnitsAtLeast(12, UnitMatchWarriors, complete = true),
        new UnitsAtLeast(1, UnitMatchCustom((unit) => unit.is(Protoss.Observer) && With.framesSince(unit.frameDiscovered) > 24 * 10), complete = true)),
      new If(
        new SafeToMoveOut,
        new PvTAttack)))

  class ReactToFiveRaxAs2GateCore extends If(
    new And(
      new EnemyStrategy(With.fingerprints.fiveRax),
      new FramesUntilUnitAtLeast(Protoss.CyberneticsCore, Protoss.Zealot.buildFrames / 3)),
    new BuildOrder(Get(2, Protoss.Zealot)))

  class ReactToWorkerRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.workerRush),
      new EnemiesAtMost(0, UnitMatchWarriors),
      new BasesAtMost(2),
      new FrameAtMost(GameTime(8, 0)())),
    new Parallel(
      new Pump(Protoss.Probe, 9),
      new If(
        new UnitsAtMost(5, UnitMatchWarriors),
        new CancelIncomplete(Protoss.Nexus)),
      new CapGasAt(50),
      new If(
        new UnitsAtLeast(10, Protoss.Probe),
        new CapGasWorkersAt(2),
        new If(
          new UnitsAtLeast(7, Protoss.Probe),
          new CapGasWorkersAt(1),
          new CapGasWorkersAt(0))),
      new Pump(Protoss.Dragoon, 3),
      new Pump(Protoss.Zealot, 3),
      new BuildOrder(
        Get(8, Protoss.Probe),
        Get(Protoss.Pylon),
        Get(10, Protoss.Probe),
        Get(Protoss.Gateway),
        Get(12, Protoss.Probe)),
      new PumpWorkers,
      new BuildHuggingNexus,
      new Build(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(2, Protoss.Pylon), Get(2, Protoss.Gateway), Get(Protoss.DragoonRange))))

  class ReactToBBS extends If(
    new And(
      new FrameAtMost(GameTime(10, 0)()),
      new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
    new Parallel(
      new If(new UnitsAtMost(5, UnitMatchWarriors), new CancelIncomplete(Protoss.Nexus)),
      new If(new UnitsAtMost(1, Protoss.Gateway), new CancelIncomplete(UnitMatchOr(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus, Protoss.Stargate))),
      new RequireSufficientSupply,
      new If(new UnitsAtLeast(2, Protoss.Reaver), new RequireMiningBases(2)),
      new Pump(Protoss.Reaver, 2),
      new If(new UnitsAtLeast(3, Protoss.Dragoon), new Build(Get(Protoss.DragoonRange))),
      new If(new UnitsAtLeast(2, Protoss.Gateway), new PumpWorkers, new PumpWorkers(cap = 12)),
      new Pump(Protoss.Dragoon),
      new Pump(Protoss.Zealot, 7),
      new Build(
        Get(Protoss.Pylon),
        Get(2, Protoss.Gateway),
        Get(18, Protoss.Probe),
        Get(Protoss.ShieldBattery)),
      new Pump(Protoss.Probe),
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.DragoonRange),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay),
        Get(2, Protoss.Nexus))))

  class ReactTo2Fac extends If(
    new And(
      new FrameAtMost(GameTime(10, 0)()),
      new EnemyStrategy(With.fingerprints.twoFac),
      new Or(
        new UnitsAtMost(0, Protoss.Stargate),
        new UnitsAtLeast(1, Protoss.RoboticsFacility))),
    new Parallel(
      new RequireSufficientSupply,
      new Pump(Protoss.Dragoon, 7),
      new Build(
        Get(9, Protoss.Probe),
        Get(Protoss.Gateway),
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(18, Protoss.Probe),
        Get(Protoss.DragoonRange),
        Get(2, Protoss.Gateway),
        Get(2, Protoss.Nexus)),
      new PumpWorkers,
      new Build(
        Get(3, Protoss.Gateway),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory))))

  class TrainMinimumDragoons extends Parallel(
    new PumpRatio(Protoss.Dragoon, 1, 5, Seq(Enemy(Terran.Vulture, 1.0), Enemy(Terran.Wraith, 1.0))),
    new PumpRatio(Protoss.Dragoon, 1, 20, Seq(Enemy(Terran.Vulture, 0.75), Enemy(Terran.Wraith, 0.5))))

  class EnemyHasMines extends Or(
    new EnemyHasShown(Terran.SpiderMine),
    new EnemyHasTech(Terran.SpiderMinePlant))

  class TrainDarkTemplar extends If(
    new And(
      // Can't spare gas on top of Carriers
      new UnitsAtMost(0, Protoss.FleetBeacon),
      new Or(
        // Drain Comsat energy in advance of Arbiters
        new Not(new Latch(new UnitsAtLeast(1, UnitMatchOr(Protoss.Arbiter, Protoss.ArbiterTribunal), complete = true))),
        //
        new And(
          new EnemiesAtMost(2, Terran.Comsat, complete = true),
          new Or(
            new Not(new EnemyHasMines),
            new And(
              new FrameAtMost(GameTime(9, 0)()),
              new EnemyStrategy(
                With.fingerprints.fiveRax,
                With.fingerprints.bbs,
                With.fingerprints.twoRax1113,
                With.fingerprints.twoFac,
                With.fingerprints.threeFac)))))))

  private class TrainObservers extends If(
    new UnitsAtLeast(24, UnitMatchWarriors),
    new Pump(Protoss.Observer, 4),
    new If(
      new UnitsAtLeast(18, UnitMatchWarriors),
      new Pump(Protoss.Observer, 3),
      new Pump(Protoss.Observer, 2)))

  class TrainReavers extends Parallel(
    new PumpRatio(Protoss.Reaver, 0, 6, Seq(
      Enemy(Terran.Marine, 1.0/6.0),
      Enemy(Terran.Goliath, 1.0/6.0),
      Enemy(Terran.Vulture, 1.0/8.0))),
    new If(
      new EnemiesAtMost(0, UnitMatchOr(Terran.Wraith, Terran.Goliath)),
      new PumpRatio(Protoss.Reaver, 1, 4, Seq(Friendly(Protoss.Shuttle, 0.5)))))

  class TrainHighTemplarAgainstBio extends If(
    new EnemyStrategy(With.fingerprints.bio),
    new PumpRatio(Protoss.HighTemplar, 1, 6, Seq(Enemy(Terran.Marine, 1.0/5.0))))

  class TrainScouts extends If(
    new And(
      new EnemiesAtMost(0, Terran.Goliath),
      new EnemiesAtMost(6, Terran.Marine),
      new EnemiesAtMost(8, Terran.MissileTurret),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Employing(PvTStove)),
    new Pump(Protoss.Scout, 5))

  class TrainZealotsOrDragoons extends Parallel(
    new PumpRatio(Protoss.Dragoon, 0, 24, Seq(Flat(6.0), Enemy(Terran.Vulture, .75))),
    new PumpRatio(Protoss.Dragoon, 0, 24, Seq(Friendly(Protoss.Zealot, 1.0))),
    new If(
      new Or(
        new And(
          new MineralsAtLeast(600),
          new GasAtMost(200)),
        new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.ZealotSpeed.upgradeFrames.head._2)),
      new PumpRatio(Protoss.Zealot, 0, 24, Seq(
        Enemy(UnitMatchSiegeTank, 2.5),
        Enemy(Terran.Goliath,     1.5),
        Enemy(Terran.Marine,      1.0),
        Enemy(Terran.Vulture,     -1.0)))),
    new Pump(Protoss.Dragoon),
    new If(new BasesAtLeast(3), new Pump(Protoss.Zealot)))

  class DarkArchonsUseful extends And(new EnemyHasShown(Terran.ScienceVessel), new UnitsAtLeast(3, Protoss.Arbiter))

  class TrainDarkArchons extends If(
    new DarkArchonsUseful,
    new If(
      new UnitsExactly(0, Protoss.DarkArchon),
      new Parallel(
        new If(new UnitsAtLeast(2, Protoss.DarkTemplar), new MeldDarkArchons),
        new If(new UnitsAtLeast(12, UnitMatchWarriors), new Pump(Protoss.DarkTemplar, 2)))))

  class TrainCarriers extends If(
    new Check(() => With.units.countEnemy(Terran.Goliath) < 8 + 3 * With.units.countOurs(Protoss.Carrier)),
    new Pump(Protoss.Carrier))

  class TrainArmy extends Parallel(
    new TrainDarkArchons,
    new If(new And(new DarkArchonsUseful, new UnitsAtLeast(2, Protoss.DarkTemplar), new UnitsAtMost(1, Protoss.DarkArchon)), new MeldDarkArchons),
    new If(new And(new DarkArchonsUseful, new UnitsAtLeast(12, UnitMatchWarriors), new UnitsAtMost(0, Protoss.DarkTemplar)), new Pump(Protoss.DarkTemplar, 2)),
    new TrainDarkTemplar,
    new PumpRatio(Protoss.Shuttle, 0, 1, Seq(Friendly(Protoss.Reaver, 1.0))),
    new PumpRatio(Protoss.Shuttle, 0, 2, Seq(Friendly(Protoss.Reaver, 0.5))),
    new TrainReavers,
    new TrainObservers,
    new TrainMinimumDragoons,
    new TrainHighTemplarAgainstBio,
    new PumpRatio(Protoss.Arbiter, 0, 2, Seq(Friendly(Protoss.Carrier, 1.0 / 8.0))),
    new If(new UnitsAtLeast(8, Protoss.Carrier), new Pump(Protoss.HighTemplar, 2)),
    new TrainCarriers,
    new Pump(Protoss.Arbiter, 6),
    new If(new GasAtLeast(500), new Pump(Protoss.HighTemplar, maximumConcurrently = 4, maximumTotal = 6)),
    new If(new GasAtLeast(1000), new Pump(Protoss.HighTemplar, maximumConcurrently = 4, maximumTotal = 10)),
    new TrainScouts,
    new TrainZealotsOrDragoons)
}

