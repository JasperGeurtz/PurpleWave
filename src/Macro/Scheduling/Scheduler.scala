package Macro.Scheduling

import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Macro.Scheduling.Optimization.ScheduleSimulationResult
import Performance.Caching.Limiter
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import Startup.With
import Utilities.{CountMap, CountMapper}

import scala.collection.mutable

class Scheduler {
  
  private val requestsByPlan = new mutable.HashMap[Plan, Iterable[BuildRequest]]
  private val recentlyUpdated = new mutable.HashSet[Plan]
  
  var simulationResults:ScheduleSimulationResult = new ScheduleSimulationResult(List.empty, List.empty, List.empty)
  
  var queueOriginal   : Iterable[Buildable]   = List.empty
  def queueOptimized  : Iterable[BuildEvent]  = simulationResults.suggestedEvents
  
  def request(requester:Plan, requests: Iterable[BuildRequest]) {
    requestsByPlan.put(requester, requests)
    recentlyUpdated.add(requester)
  }
  
  def onFrame() = {
    requestsByPlan.keySet.diff(recentlyUpdated).foreach(requestsByPlan.remove)
    recentlyUpdated.clear()
    updateQueueLimiter.act()
  }
  private val updateQueueLimiter = new Limiter(2, () => updateQueue())
  private def updateQueue() {
    val requestQueue = requestsByPlan.keys.toList.sortBy(With.prioritizer.getPriority).flatten(requestsByPlan)
    val unitsWanted = new CountMap[UnitClass]
    val unitsActual:CountMap[UnitClass] = CountMapper.make(With.units.ours.groupBy(_.unitClass).mapValues(_.size))
    queueOriginal = requestQueue.flatten(buildable => getUnfulfilledBuildables(buildable, unitsWanted, unitsActual))
    //simulationResults = ScheduleSimulator.simulate(queueOriginal)
  }
  
  private def getUnfulfilledBuildables(
    request:BuildRequest,
    unitsWanted:CountMap[UnitClass],
    unitsActual:CountMap[UnitClass]):Iterable[Buildable] = {
    if (request.buildable.upgradeOption.nonEmpty) {
      if(With.self.getUpgradeLevel(request.buildable.upgradeOption.get.base) >= request.buildable.upgradeLevel)
        return List(request.buildable)
      else
        return None
    }
    else if (request.buildable.techOption.nonEmpty) {
      if (With.self.hasResearched(request.buildable.techOption.get.base))
        return List(request.buildable)
      else
        return None
    }
    else {
      val unit = request.buildable.unitOption.get
      unitsWanted.add(unit, request.add)
      unitsWanted.put(unit, Math.max(request.require, unitsWanted(unit)))
      val difference = unitsWanted(unit) - unitsActual(unit)
      if (difference > 0) {
        val buildables = (0 until difference).map(i => request.buildable)
        return buildables
      }
      else
        return List.empty
    }
  }
}