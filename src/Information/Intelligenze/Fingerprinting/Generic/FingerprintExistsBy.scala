package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher

class FingerprintExistsBy(unitMatcher: UnitMatcher, gameTime: GameTime, quantity: Int = 1) extends Fingerprint {
  override def investigate: Boolean = With.units.countEverP(_.is(unitMatcher)) >= quantity
  override val sticky: Boolean = true
}
