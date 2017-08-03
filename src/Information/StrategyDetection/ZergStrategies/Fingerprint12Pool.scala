package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, GameTime}
import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object Fingerprint12Pool extends FingerprintAnd(
  FingerprintNot(Fingerprint4Pool),
  FingerprintNot(Fingerprint9Pool),
  FingerprintNot(FingerprintOverpool),
  FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40))
)
