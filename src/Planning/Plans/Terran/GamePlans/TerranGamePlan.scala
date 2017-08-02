package Planning.Plans.Terran.GamePlans

import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace

class TerranGamePlan
  extends Parallel (
    new SwitchEnemyRace {
      terran  .set(new TerranVsTerran)
      protoss .set(new TerranVsZerg)
      zerg    .set(new TerranVsZerg)
      random  .set(new TerranVsZerg)
    }
  )
