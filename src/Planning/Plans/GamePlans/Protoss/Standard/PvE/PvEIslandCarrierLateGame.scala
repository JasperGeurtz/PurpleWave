package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{Get, GetAnother}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import Planning.Predicates.Milestones.{EnemyHasShown, OnGasPumps, UnitsAtLeast, UpgradeComplete}
import Planning.Predicates.Reactive.EnemyMutalisks
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Plasma
import Utilities.ByOption

class PlaceIslandPylons extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(this, building = Some(Protoss.Pylon), preferZone =
      ByOption.minBy(
        With.geography.bases
          .filter(b => b != With.geography.ourMain && b != With.geography.ourNatural)
          .map(_.zone))(_.centroid.groundPixels(With.geography.home)))
)}

class IslandCarrierUpgrades extends Parallel(
  new If(
    new Or(
      new UnitsAtLeast(2, Protoss.Carrier),
      new UnitsAtLeast(1, Protoss.Carrier, complete = true)),
    new UpgradeContinuously(Protoss.CarrierCapacity)),
  new If(
    new UnitsAtLeast(2, Protoss.CyberneticsCore, complete = true),
    new Parallel(
      new UpgradeContinuously(Protoss.AirDamage),
      new UpgradeContinuously(Protoss.AirArmor))),
    new Parallel(
      new UpgradeContinuously(Protoss.AirDamage, 1),
      new If(
        new UpgradeComplete(Protoss.AirDamage, 3),
        new UpgradeContinuously(Protoss.AirArmor, 3),
        new If(
          new UpgradeComplete(Protoss.AirArmor, 2),
          new UpgradeContinuously(Protoss.AirDamage, 3),
          new If(
            new UpgradeComplete(Protoss.AirDamage, 2),
            new UpgradeContinuously(Protoss.AirArmor, 2),
            new If(
              new UpgradeComplete(Protoss.AirArmor, 1),
              new UpgradeContinuously(Protoss.AirDamage, 2),
              new If(
                new UpgradeComplete(Protoss.AirDamage, 1),
                new UpgradeContinuously(Protoss.AirArmor, 1),
                new UpgradeContinuously(Protoss.AirDamage, 1))))))))

class ExpandOverIsland(maxBases: Int) extends RequireBases {
  basesDesired.set(
    if (Plasma.matches)
      3
    else
      Math.min(maxBases, With.geography.bases.count(_.zone.canWalkTo(With.geography.ourMain.zone))))
}

class PvEIslandCarrierLateGame extends Parallel(
  // Prerequisites
  new Build(
    Get(Protoss.Gateway),
    Get(Protoss.Assimilator),
    Get(Protoss.CyberneticsCore),
    Get(Protoss.Stargate)),
  new BuildGasPumps,

  // Corsairs
  new If(
    new EnemyMutalisks,
    new Parallel(
      new Pump(Protoss.Corsair, 8),
      new BuildTowersAtBases(2),
      new Build(Get(2, Protoss.Stargate)))),
  new PumpRatio(Protoss.Corsair, 0, 24, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Terran.Wraith, 0.5))),

  // Observers
  new Trigger(
    new Or(
      new EnemyHasShown(Terran.Wraith),
      new EnemyHasShown(Protoss.Arbiter),
      new EnemyHasShown(Protoss.ArbiterTribunal)),
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory),
      Get(2, Protoss.Observer))),

  new If(
    new EnemyMutalisks,
    new IslandCarrierUpgrades),

  new Build(Get(Protoss.FleetBeacon)),

  new Trigger(
    new UnitsAtLeast(1, Protoss.FleetBeacon),
    new Parallel(
      new BuildOrder(
        Get(2, Protoss.Stargate),
        Get(2, Protoss.Carrier)),
      new IslandCarrierUpgrades,
      new Build(Get(Protoss.Forge)),
      new BuildTowersAtBases(1),

      // Expansions
      new If(new UnitsAtLeast(4, Protoss.Carrier), new RequireMiningBases(2)),
      new If(new UnitsAtLeast(8, Protoss.Carrier), new RequireMiningBases(3)),
      new If(new UnitsAtLeast(4, Protoss.Carrier), new PumpWorkers),

      new Pump(Protoss.Carrier),

      // Extended macro
      new Build(Get(2, Protoss.CyberneticsCore)),
      new OnGasPumps(1, new Build(Get(3, Protoss.Stargate))),
      new OnGasPumps(2, new Build(Get(5, Protoss.Stargate))),
      new OnGasPumps(3, new Build(Get(8, Protoss.Stargate))),
      new OnGasPumps(4, new Build(Get(12, Protoss.Stargate))),
      new ExpandOverIsland(12),
      new If(
        new UnitsAtLeast(2, Protoss.Stargate, complete = true),
        new Build(
          GetAnother(8, Protoss.PhotonCannon),
          GetAnother(2, Protoss.Pylon))))),
)
