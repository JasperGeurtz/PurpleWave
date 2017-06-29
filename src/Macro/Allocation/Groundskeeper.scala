package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{BuildingDescriptor, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class Groundskeeper {
  
  case class RequirementMatch(requirement: BuildingDescriptor, proposal: BuildingDescriptor)
  
  val updated               : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val proposals             : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val proposalPlacements    : mutable.Map[BuildingDescriptor, Placement]  = new mutable.HashMap[BuildingDescriptor, Placement]
  val lastPlacementAttempt  : mutable.Map[BuildingDescriptor, Int]        = new mutable.HashMap[BuildingDescriptor, Int]
  val requirementMatches    : mutable.Set[RequirementMatch]               = new mutable.HashSet[RequirementMatch]
  
  ///////////
  // Tasks //
  ///////////
  
  def update() {
    proposals.diff(updated).foreach(removeDescriptor)
    proposalPlacements.keySet.diff(updated).foreach(removeDescriptor)
    requirementMatches.map(_.requirement).diff(updated).foreach(removeDescriptor)
    updated.clear()
  }
  
  def placeBuildings() {
    var newSearches = 0
    With.architect.reboot()
    proposalsInUpdateOrder
      .foreach(descriptor =>
        if (With.performance.continueRunning && newSearches < With.configuration.maxGroundskeeperSearches) {
          val previousPlacement = proposalPlacements.get(descriptor)
          if (previousPlacement.isEmpty) {
            newSearches += 1
          }
          lastPlacementAttempt(descriptor) = With.frame
          val placement = With.architect.fulfill(descriptor, previousPlacement)
          if (placement.tile.isDefined) {
            proposalPlacements.put(descriptor, placement)
          }
          else {
            proposalPlacements.remove(descriptor)
          }
        })
  }
  
  private def removeDescriptor(descriptor: BuildingDescriptor) {
    proposals.remove(descriptor)
    proposalPlacements.remove(descriptor)
    lastPlacementAttempt.remove(descriptor)
    requirementMatches
      .filter(m => m.requirement == descriptor || m.proposal == descriptor)
      .foreach(requirementMatches.remove)
  }
  
  private def proposalsInUpdateOrder: Iterable[BuildingDescriptor] = {
    // Verify existing proposals
    // in priority order (top priority first)
    // Then place upcoming proposals
    // ordered by time since last attempt at placement
    // then by priority
    val placed = proposalPlacements.keySet
    val unplaced = proposals.diff(placed)
    val ordered =
      placed.toList
        .sortBy(_.proposer.priority) ++
      unplaced.toList
        .sortBy(_.proposer.priority)
        .sortBy(lastPlacementAttempt.getOrElse(_, 0))
    ordered
  }
  
  //////////////
  // Plan API //
  //////////////
  
  /*
  Propose an idea for building placement.
  Intended for use by Plans that don't actually care when the building gets placed.
  This is just to say "hey, it would be nice if a matching building got built here."
  */
  def propose(proposal: BuildingDescriptor) {
    flagUpdated(proposal)
    addProposal(proposal)
  }
  
  /*
  Require placement of a building matching the specified criteria.
  Intended for use by Plans that urgently need a building placement.
  If a matching proposal (specified by the above propose()) is available, they'll use that.
  
  So if you want to build a Pylon and don't really care where,
  but someone proposed a specific place for a Pylon,
  use the previously proposed place.
   */
  def require(requirement: BuildingDescriptor): Option[Tile] = {
    flagUpdated(requirement)
    addRequirement(requirement)
    getTileForRequirement(requirement)
  }
  
  //////////////
  // Internal //
  //////////////
  
  private def flagUpdated(descriptor: BuildingDescriptor) {
    updated.add(descriptor)
  }
  private def addProposal(proposal: BuildingDescriptor) {
    proposals.add(proposal)
  }
  
  private def addRequirement(requirement: BuildingDescriptor) {
    val proposal = proposals
      .diff(requirementMatches.map(_.proposal))
      .find(requirement.fulfilledBy)
      .getOrElse(requirement)
    if (proposal == requirement) {
      addProposal(requirement)
    }
    requirementMatches.add(RequirementMatch(requirement = requirement, proposal = proposal))
  }
  
  private def getTileForRequirement(requirement: BuildingDescriptor): Option[Tile] = {
    val proposal = getProposalForRequirement(requirement)
    flagUpdated(proposal)
    proposalPlacements.get(proposal).flatMap(_.tile)
  }
  
  private def getProposalForRequirement(requirement: BuildingDescriptor): BuildingDescriptor = {
    requirementMatches.find(_.requirement == requirement).get.proposal
  }
}
