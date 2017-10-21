package Information.Intelligence.Fingerprinting.Generic

import Information.Intelligence.Fingerprinting.Fingerprint
import Lifecycle.With

class FingerprintScoutedEnemyBases(count: Int) extends Fingerprint {
  override def investigate: Boolean = With.geography.enemyBases.count(_.scouted) >= count
  
}
