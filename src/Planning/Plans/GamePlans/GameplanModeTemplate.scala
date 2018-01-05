package Planning.Plans.GamePlans

import Macro.Architecture.Blueprint
import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.NoPlan
import Planning.Plans.GamePlans.Protoss.Situational.DefendAgainstProxy
import Planning.Plans.Macro.Automatic.{Gather, MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}

abstract class GameplanModeTemplate extends GameplanMode {
  
  def meldArchonsAt         : Int               = 40
  def aggression            : Double            = 1.0
  def removeMineralBlocksAt : Int               = 60
  def scoutAt               : Int               = 14
  def scoutExpansionsAt     : Int               = 60
  def superSaturate         : Boolean           = false
  def blueprints            : Seq[Blueprint]    = Seq.empty
  def buildOrder            : Seq[BuildRequest] = Vector.empty
  def emergencyPlans        : Seq[Plan]         = Vector.empty
  def buildPlans            : Seq[Plan]         = Vector.empty
  def defaultAggressionPlan : Plan              = new Aggression(aggression)
  def defaultPlacementPlan  : Plan              = new ProposePlacement(blueprints: _*)
  def defaultArchonPlan     : Plan              = new MeldArchons(meldArchonsAt)
  def defaultSupplyPlan     : Plan              = new RequireSufficientSupply
  def defaultWorkerPlan     : Plan              = new TrainWorkersContinuously(superSaturate)
  def defaultScoutPlan      : Plan              = new ScoutAt(scoutAt)
  def priorityDefensePlan   : Plan              = NoPlan()
  def priorityAttackPlan    : Plan              = NoPlan()
  def defaultNukePlan       : Plan              = new NukeBase
  def defaultAttackPlan     : Plan              = new ConsiderAttacking
  def defaultDropPlan       : Plan              = new DropAttack
  
  def defaultMacroPlans: Vector[Plan] = Vector(
    defaultArchonPlan,
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(removeMineralBlocksAt))
  
  def defaultTacticsPlans: Vector[Plan] = Vector(
    defaultAggressionPlan,
    priorityDefensePlan,
    priorityAttackPlan,
    defaultDropPlan,
    defaultScoutPlan,
    new DefendZones,
    new DefendAgainstProxy,
    new EscortSettlers,
    new ScoutExpansionsAt(scoutExpansionsAt),
    defaultNukePlan,
    defaultAttackPlan,
    new DefendEntrance,
    new Gather,
    new RecruitFreelancers,
    new Scan
  )
  
  private var initialized = false
  override def onUpdate() {
    if ( ! initialized) {
      initialized = true
      children.set(
        Vector(defaultPlacementPlan)
          ++ Vector(new RequireEssentials)
          ++ emergencyPlans
          ++ Vector(new BuildOrder(buildOrder: _*))
          ++ Vector(defaultSupplyPlan)
          ++ Vector(defaultWorkerPlan)
          ++ buildPlans
          ++ defaultMacroPlans
          ++ defaultTacticsPlans
      )
    }
    super.onUpdate()
  }
  
}
