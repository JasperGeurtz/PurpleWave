package Information.Intelligence.Fingerprinting.ZergStrategies

import Information.Intelligence.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint4Pool extends FingerprintOr(
  new FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 50)),
  new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))) {
  
  trigger = true
}
