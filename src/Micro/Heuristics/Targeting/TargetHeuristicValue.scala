package Micro.Heuristics.Targeting

import Micro.Execution.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicValue extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
  
    candidate.subjectiveValue
    
  }
}
