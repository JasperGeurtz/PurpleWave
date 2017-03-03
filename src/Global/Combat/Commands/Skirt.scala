package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Skirt extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val threat = With.grids.enemyGroundStrength.get(unit.position)
    
    if (threat > 0) {
      Dodge.execute(intent)
    } else {
      Engage.execute(intent)
    }
  }
}
