package Planning.Plans.Army

import Debugging.Visualizations.Rendering.DrawMap
import Micro.Intent.Intention
import Planning.Composition.PixelFinders.TileFinder
import Planning.Composition.PixelFinders.Tactics.TileEnemyBase
import Planning.Composition.Property
import Planning.Plan
import Planning.Composition.ResourceLocks.LockUnits
import Lifecycle.With
import Utilities.EnrichPixel._

class ControlPixel extends Plan {
  
  description.set("Control a position")
  
  val infiltrationRadius = 32.0 * 25
  
  val units = new Property[LockUnits](new LockUnits)
  var positionToControl = new Property[TileFinder](new TileEnemyBase)
  
  override def update() {
    
    var targetPixel = positionToControl.get.find.get
    
    val ourBases = With.geography.ourBases.map(_.townHallArea.midPixel)
    val infiltrators = With.units.enemy
      .filter(e =>
        e.possiblyStillThere &&
        e.canAttackThisFrame &&
        ourBases.exists(base =>
          e.travelPixels(base.tileIncluding) < infiltrationRadius &&
          e.travelPixels(base.tileIncluding) <
          e.travelPixels(base.tileIncluding, targetPixel)))
        
    if (infiltrators.nonEmpty) {
      targetPixel = infiltrators.map(_.tileIncludingCenter).minBy(_.tileDistance(With.geography.home))
    }
    
    units.get.acquire(this)
    if (units.get.satisfied) {
      //TODO: Dispatch only units capable of fighting an infiltrator
      
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = Some(targetPixel) }))
    }
  }
  
  override def drawOverlay() {
    
    positionToControl.get.find.map(tile => {
      DrawMap.circle(
        tile.pixelCenter,
        64,
        With.self.colorDark)
      
      DrawMap.label(
        description.get,
        tile.pixelCenter,
        drawBackground = true,
        With.self.colorDark)
    })
  }
}
