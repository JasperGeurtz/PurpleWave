package Information.Intelligence.Fingerprinting.Generic

import Information.Intelligence.Fingerprinting.Fingerprint

class FingerprintAnd(fingerprints: Fingerprint*) extends Fingerprint {
  
  override def investigate: Boolean = {
    fingerprints.foreach(_.matches) // Hack -- we need all fingerprints to update each time.
    fingerprints.forall(_.matches)
  }
}
