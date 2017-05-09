package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{And, IfThenElse, Parallel}
import Planning.Plans.Information.{FindExpansions, ScoutAt}
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.Milestones.{HaveUpgrade, SupplyAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  private val _thirdGateway = Vector[BuildRequest] (
    RequestUnitAtLeast(3, Protoss.Gateway)
  )
  
  private val _firstExpansion = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Nexus)
  )
  
  private val _lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(2,   Protoss.Nexus),
    RequestUnitAtLeast(2,   Protoss.Assimilator),
    RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestUnitAtLeast(7,   Protoss.Gateway),
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(3,   Protoss.Assimilator),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(9,   Protoss.Gateway),
    RequestUpgrade(         Protoss.ZealotSpeed),
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(4,   Protoss.Assimilator),
    RequestUnitAtLeast(12,  Protoss.Gateway),
    RequestUnitAtLeast(5,   Protoss.Nexus),
    RequestUnitAtLeast(5,   Protoss.Assimilator),
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Assimilator),
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Assimilator)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(ProtossBuilds.OpeningOneGateCore),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new IfThenElse(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new Parallel(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot)),
        new ScheduleBuildOrder(ProtossBuilds.TakeNatural)
      )
    ),
    new IfThenElse(
      new And(
        new UnitsAtLeast(10, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver))
      ),
      new ScheduleBuildOrder(ProtossBuilds.TakeThirdBase)
    ),
    new TrainContinuously(Protoss.Reaver, 4),
    new IfThenElse(
      new And(
        new HaveUpgrade(Protoss.ZealotSpeed),
        new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
      new TrainContinuously(Protoss.Zealot),
      new TrainContinuously(Protoss.Dragoon)
    ),
    new ScheduleBuildOrder(_thirdGateway),
    new ScheduleBuildOrder(ProtossBuilds.TechDragoons),
    new ScheduleBuildOrder(ProtossBuilds.TechReavers),
    new ScheduleBuildOrder(_lateGame),
    new IfThenElse(
      new SupplyAtLeast(140),
      new FindExpansions
    ),
    new ScoutAt(9),
    new Attack
  ))
}
