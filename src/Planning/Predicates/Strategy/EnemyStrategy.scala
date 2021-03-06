package Planning.Predicates.Strategy

import Information.Intelligenze.Fingerprinting.Fingerprint
import Planning.Predicate

class EnemyStrategy(fingerprints: Fingerprint*) extends Predicate {
  
  override def isComplete: Boolean = fingerprints.exists(_.matches)
}
