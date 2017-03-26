package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val defaultCombat = new MovementProfile (
    preferTravel      = 2.0,
    preferSitAtRange  = 1.0,
    preferTarget      = 1.5,
    preferMobility    = 1.0,
    preferStrength    = 2.0,
    avoidDamage       = 0.25,
    avoidTraffic      = 0.75)
  
  val defaultNormal = new MovementProfile (
    preferTravel      = 2.0,
    preferRandom      = 0.05,
    preferMoving      = 0.05,
    avoidTraffic      = 1.0)
  
  val worker = new MovementProfile (
    preferSpot        = 2.5,
    preferRandom      = 0.05,
    avoidDamage       = 1.0)
  
  val darkTemplar = new MovementProfile (
    preferTravel      = 1.0,
    preferSpot        = 1.0,
    avoidDamage       = 1.0,
    avoidDetection    = 3.0)
  
  val carrier = new MovementProfile (
    preferTravel      = 2.0,
    preferSitAtRange  = 2.0,
    preferStrength    = 0.1,
    avoidDamage       = 1.0)
  
  val corsair = new MovementProfile (
    preferTravel      = 2.0,
    preferTarget      = 1.0,
    preferStrength    = 0.05,
    avoidDamage       = 0.5)
}
