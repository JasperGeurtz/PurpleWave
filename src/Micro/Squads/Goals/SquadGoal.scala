package Micro.Squads.Goals

import Micro.Squads.RecruitmentLevel.RecruitmentLevel
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait SquadGoal {
  var squad: Squad = _
  
  def run()
  def prepareForCandidates() {}
  def offer(candidates: Iterable[FriendlyUnitInfo], recruitmentNeed: RecruitmentLevel)
  
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}
