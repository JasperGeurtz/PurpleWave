package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Micro.Agency.Intention
import Planning.Plan
import ProxyBwapi.Races.Zerg

class ExtractorTrick extends Plan {
  
  override def onUpdate() {
    
    lazy val extractors = With.units.ours.filter(e => e.is(Zerg.Extractor) && ! e.complete)
    val shouldBuildExtractor = (
      With.self.supplyTotal == 18
      && With.self.supplyUsed == 18
      && With.self.minerals >= 76
      && With.units.existsOurs(Zerg.Larva)
      && extractors.isEmpty)
  
    lazy val shouldCancelExtractor = (
      // Give time for our supply to update
      extractors.exists(e => With.framesSince(e.frameDiscovered) > 5 * 24)
      && (extractors.exists(_.remainingCompletionFrames < 3 * 24)
        || (
          With.self.supplyTotal == 18
          && With.self.supplyUsed >= 18)))
    
    if (shouldBuildExtractor) {
      With.scheduler.request(this, RequestAtLeast(1, Zerg.Extractor))
    }
    else if (shouldCancelExtractor) {
      extractors.foreach(unit => {
        val intent = new Intention
        intent.canCancel = true
        unit.agent.intend(this, intent)
      })
    }
  }
  
}