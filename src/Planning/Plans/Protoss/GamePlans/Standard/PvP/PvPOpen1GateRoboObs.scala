package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.Architecture.Blueprint
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Always
import Planning.Plans.Information.Reactive.EnemyBasesAtLeast
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast}
import Planning.Plans.Protoss.Situational.Blueprinter
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Protoss

class PvPOpen1GateRoboObs extends Mode {
  
  override val activationCriteria: Plan = new Always //Employing(PvPOpeningDarkTemplar)
  
  override val completionCriteria: Plan = new Or(
    new EnemyBasesAtLeast(2),
    new UnitsAtLeast(1, Protoss.Observer))
  
  private class ProposeCannonsAtExpanion extends ProposePlacement {
    override lazy val blueprints: Iterable[Blueprint] = Blueprinter.pylonsAndCannonsAtNatural(this, 1, 3)
  }
  
  children.set(Vector(
    new RequireBareMinimum,
    new TrainContinuously(Protoss.Observer, 1), // Make darn sure we get it out ASAP
    new If(
      new EnemyUnitsAtLeast(1, Protoss.DarkTemplar),
      new Build(RequestAtLeast(3, Protoss.PhotonCannon))),
    new BuildOrder(
      // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
      // We get gas/core faster because of mineral locking + later scout
      RequestAtLeast(8,   Protoss.Probe),
      RequestAtLeast(1,   Protoss.Pylon),             // 8
      RequestAtLeast(10,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.Gateway),           // 10
      RequestAtLeast(11,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.Assimilator),       // 11
      RequestAtLeast(13,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.CyberneticsCore),
      RequestAtLeast(14,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.Zealot),
      RequestAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
      RequestAtLeast(16,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.Dragoon),           // 18 = 16 + Z
      RequestUpgrade(Protoss.DragoonRange),           // 20 = 16 + Z + D
      RequestAtLeast(17,  Protoss.Probe),
      RequestAtLeast(3,   Protoss.Pylon),             // 21 = 17 + Z + D
      RequestAtLeast(18,  Protoss.Probe),
      RequestAtLeast(2,   Protoss.Dragoon),           // 22 = 18 + Z + D
      RequestAtLeast(20,  Protoss.Probe),
      RequestAtLeast(1,   Protoss.RoboticsFacility),  // 26 = 20 + Z + DD
      RequestAtLeast(21,  Protoss.Probe),             // Now probe cut
      RequestAtLeast(3,   Protoss.Dragoon),           // TL is unclear whether this should be a Dragoon or two more Probes
      RequestAtLeast(3,   Protoss.Gateway),           // 29 = 21 + Z + DDD
      RequestAtLeast(4,   Protoss.Dragoon),           // 29 = 21 + Z + DDD
      RequestAtLeast(22,  Protoss.Probe),
      RequestAtLeast(4,   Protoss.Pylon),
      RequestAtLeast(23,  Protoss.Probe),
      RequestAtLeast(5,   Protoss.Dragoon),
      RequestAtLeast(1,   Protoss.Observatory)        // 33 = 23 + Z + DDDD
    ),
    
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new TrainContinuously(Protoss.Observer, 1),
    new TrainContinuously(Protoss.Dragoon),
  
    new RequireMiningBases(2),
    new Build(RequestAtLeast(4, Protoss.Gateway)),
      
    new Trigger(
      new UnitsAtLeast(1, Protoss.CyberneticsCore),
      new Scout)
  ))
}
