package ProxyBwapi.UnitInfo

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.PurpleMath
import Micro.Matchups.MatchupAnalysis
import Performance.Caching.CacheFrame
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import bwapi._

import scala.collection.mutable

abstract class UnitInfo (base: bwapi.Unit) extends UnitProxy(base) {
  
  //////////////
  // Identity //
  //////////////
  
  val frameDiscovered: Int = With.frame
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString: String = {
    unitClass.toString + " #" +
    id + " " +
    hitPoints + "/" + unitClass.maxHitPoints + " " +
    (if (shieldPoints > 0) "(" + shieldPoints + "/" + unitClass.maxShields + ") " else "") +
    tileIncludingCenter.toString + " " + pixelCenter.toString
  }
  
  def is(unitClasses: UnitClass*):Boolean = unitClasses.contains(unitClass)
  
  //////////////////
  // Statefulness //
  //////////////////
  
  var completionFrame: Int = frameDiscovered + remainingBuildFrames

  private val history = new mutable.Queue[UnitState]
  def update() {
    if ( ! complete) {
      completionFrame = frameDiscovered + remainingBuildFrames
    }
    
    if (history.headOption.exists(_.frame == With.frame)) {
      // Game is paused; we don't know how to clear the queue
      return
    }
      
    while (history.headOption.exists(_.age > With.configuration.unitHistoryAge)) {
      history.dequeue()
    }
    history.enqueue(new UnitState(this))
  }
  
  def lastAttackStartFrame: Int = {
    val attackStartingStates = history.filter(_.attackStarting)
    if (attackStartingStates.isEmpty)
      0
    else
      attackStartingStates.map(_.frame).max
  }
  
  def damageInLastSecond: Int = damageInLastSectiondCache.get
  
  private val damageInLastSectiondCache = new CacheFrame(() =>
    Math.max(
      0,
      history
        .filter(_.age > 24)
        .lastOption
        .map(lastState =>
          lastState.hitPoints             - hitPoints     +
          lastState.shieldPoints          - shieldPoints  +
          lastState.defensiveMatrixPoints - defensiveMatrixPoints)
        .getOrElse(0)))
  
  private lazy val stuckMoveFrames     = 12
  private lazy val stuckAttackFrames = cooldownMaxAirGround + 8
  private lazy val stuckFramesMax    = Math.max(stuckMoveFrames, stuckAttackFrames)
  def seeminglyStuck: Boolean = {
    val recentHistory = history.takeRight(stuckFramesMax)
    history.size >= stuckFramesMax && (
      history.takeRight(stuckMoveFrames   ).forall(state => state.couldMoveThisFrame    && state.tryingToMove   && state.pixelCenter == pixelCenter) ||
      history.takeRight(stuckAttackFrames ).forall(state => state.couldAttackThisFrame  && state.tryingToAttack && state.pixelCenter == pixelCenter && state.cooldown == 0)
    )
  }
  
  ////////////
  // Health //
  ////////////
  
  def aliveAndComplete:Boolean = alive && complete
  
  def energyMax     : Int = unitClass.maxEnergy //TODO: Add upgrades
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  def wounded:Boolean = totalHealth < Math.min(With.configuration.woundedThresholdHealth, unitClass.maxTotalHealth/3)
  
  ///////////////
  // Economics //
  ///////////////
  
  def subjectiveValue: Int = unitClass.subjectiveValue + scarabCount * Protoss.Scarab.subjectiveValue + interceptorCount * Protoss.Interceptor.subjectiveValue
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.x
  def y: Int = pixelCenter.y
  
  def tileIncludingCenter:  Tile          = pixelCenter.tileIncluding
  def tileArea:             TileRectangle = unitClass.tileArea.add(tileTopLeft)
  
  def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  def pixelRangeAir: Double = pixelRangeAirCache.get
  private val pixelRangeAirCache = new CacheFrame(() =>
    unitClass.airRange +
      (if (is(Terran.Bunker))                                                 32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Goliath)   && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  
  def pixelRangeGround: Double = pixelRangeGroundCache.get
  private val pixelRangeGroundCache = new CacheFrame(() =>
    unitClass.groundRange +
      (if (is(Terran.Bunker))                                               32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  
  def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)
  
  def canTraverse             (tile:        Tile)       : Boolean = flying || With.grids.walkable.get(tile)
  def pixelsFromEdgeSlow      (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelsFromEdgeFast      (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelDistanceSlow       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSlow(otherPixel)
  def pixelDistanceSlow       (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit.pixelCenter)
  def pixelDistanceFast       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceFast(otherPixel)
  def pixelDistanceFast       (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherUnit:   UnitInfo)   : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def pixelDistanceTravelling (destination: Pixel)      : Double  = pixelDistanceTravelling(pixelCenter, destination)
  def pixelDistanceTravelling (destination: Tile)       : Double  = pixelDistanceTravelling(pixelCenter, destination.pixelCenter)
  def pixelDistanceTravelling (from: Pixel, to: Pixel)  : Double  = if (flying) from.pixelDistanceFast(to) else from.groundPixels(to)
  
  def canMove: Boolean = canMoveCache.get
  private val canMoveCache = new CacheFrame(() => unitClass.canMove && topSpeed > 0 && canDoAnything && ! burrowed)
  
  def topSpeedChasing: Double = topSpeedChasingCache.get
  private val topSpeedChasingCache = new CacheFrame(() => topSpeed * PurpleMath.nanToOne(Math.max(0, cooldownMaxAirGround - unitClass.stopFrames) / cooldownMaxAirGround.toDouble))
  
  def topSpeed: Double = topSpeedCache.get
  private val topSpeedCache = new CacheFrame(() =>
    (if (stimmed) 1.5 else 1.0) * (
    unitClass.topSpeed * (if (
      (is(Terran.Vulture)   && player.getUpgradeLevel(Terran.VultureSpeed)    > 0) ||
      (is(Protoss.Observer) && player.getUpgradeLevel(Protoss.ObserverSpeed)  > 0) ||
      (is(Protoss.Scout)    && player.getUpgradeLevel(Protoss.ScoutSpeed)     > 0) ||
      (is(Protoss.Shuttle)  && player.getUpgradeLevel(Protoss.ShuttleSpeed)   > 0) ||
      (is(Protoss.Zealot)   && player.getUpgradeLevel(Protoss.ZealotSpeed)    > 0) ||
      (is(Zerg.Overlord)    && player.getUpgradeLevel(Zerg.OverlordSpeed)     > 0) ||
      (is(Zerg.Zergling)    && player.getUpgradeLevel(Zerg.ZerglingSpeed)     > 0) ||
      (is(Zerg.Hydralisk)   && player.getUpgradeLevel(Zerg.HydraliskSpeed)    > 0) ||
      (is(Zerg.Ultralisk)   && player.getUpgradeLevel(Zerg.UltraliskSpeed)    > 0))
      1.5 else 1.0)))
  
  def project(distance: Double)       : Pixel = pixelCenter.radiateRadians(angleRadians, distance)
  def project(framesToLookAhead: Int) : Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)
  
  def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  ////////////
  // Combat //
  ////////////
  
  def battle: Option[Battle] = With.battles.byUnit.get(this)
  def matchups: MatchupAnalysis = With.matchups.get(this)
  
  def ranged  : Boolean = unitClass.rawCanAttack && unitClass.maxAirGroundRange > 32 * 2
  def melee   : Boolean = unitClass.rawCanAttack && ! ranged
  
  def armorHealth: Int = armorHealthCache.get
  def armorShield: Int = armorShieldsCache.get
  
  lazy val armorHealthCache   = new CacheFrame(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  lazy val armorShieldsCache  = new CacheFrame(() => player.getUpgradeLevel(Protoss.Shields))
  
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  
  def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  
  def attacksGround : Boolean = unitClass.attacksGround
  def attacksAir    : Boolean = unitClass.attacksAir
  
  def airDpf    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir
  def groundDpf : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround
  
  def attacksAgainstAir: Int = attacksAgainstAirCache.get
  private val attacksAgainstAirCache = new CacheFrame(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = interceptorCount
    output
  })
  
  def attacksAgainstGround: Int = attacksAgainstGroundCache.get
  private val attacksAgainstGroundCache = new CacheFrame(() => {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = interceptorCount
    if (output == 0  && is(Protoss.Reaver))   output = 1
    output
  })
  
  def cooldownLeft      : Int = Math.max(airCooldownLeft, groundCooldownLeft)
  def cooldownMaxAir    : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  def cooldownMaxGround : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG
  
  def cooldownMaxAirGround: Int = Math.max(
    if (attacksAir)     cooldownMaxAir    else 0,
    if (attacksGround)  cooldownMaxGround else 0)
  
  def cooldownMaxAgainst(enemy: UnitInfo): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround
  
  def pixelRangeAgainstFromEdge   (enemy: UnitInfo): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  def pixelRangeAgainstFromCenter (enemy: UnitInfo): Double = pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse
  
  def missChanceAgainst(enemy: UnitInfo): Double = {
    if (guaranteedToHit(enemy)) 0.53 else 0.0
  }
  def guaranteedToHit(enemy: UnitInfo): Boolean =
    flying                          ||
    enemy.flying                    ||
    unitClass.unaffectedByDarkSwarm ||
    With.grids.altitudeBonus.get(tileIncludingCenter) >= With.grids.altitudeBonus.get(enemy.tileIncludingCenter)
  
  def damageTypeAgainst (enemy: UnitInfo)  : DamageType  = if (enemy.flying) unitClass.airDamageTypeRaw else unitClass.groundDamageTypeRaw
  def attacksAgainst    (enemy: UnitInfo)  : Int         = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  
  def damageScaleAgainstHitPoints(enemy: UnitInfo): Double =
    if (enemy.flying && airDpf > 0)
      Damage.scaleBySize(unitClass.airDamageTypeRaw, enemy.unitClass.size)
    else if (groundDpf > 0)
      Damage.scaleBySize(unitClass.groundDamageTypeRaw, enemy.unitClass.size)
    else
      0.0
  
  def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  def damageOnHitGround   : Int = damageOnHitGroundCache.get
  def damageOnHitAir      : Int = damageOnHitAirCache.get
  private val damageOnHitGroundCache  = new CacheFrame(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new CacheFrame(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  
  def damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  def damageOnNextHitAgainst(enemy: UnitInfo): Int = {
    damageOnNextHitAgainst(enemy, enemy.shieldPoints)
  }
  
  def damageOnNextHitAgainst(enemy: UnitInfo, enemyShields: Int): Int = {
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo)
    val damageScale             = damageScaleAgainstHitPoints(enemy)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShields + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealth          = (damageAssignedToHealth - enemy.armorHealth * hits) * damageScaleAgainstHitPoints(enemy)
    val damageDealtTotal        = damageAssignedToHealth + damageAssignedToShields
    Math.max(1, missChanceAgainst(enemy) * damageDealtTotal).toInt
  }
  
  def dpfOnNextHitAgainst(enemy: UnitInfo): Double = {
    if (unitClass.suicides) {
      damageOnNextHitAgainst(enemy)
    }
    else {
      val cooldownVs = cooldownMaxAgainst(enemy)
      if (cooldownVs == 0)
        0.0
      else
        damageOnNextHitAgainst(enemy).toDouble / cooldownVs
    }
  }
  
  def canDoAnything: Boolean = canDoAnythingCache.get
  private val canDoAnythingCache = new CacheFrame(() =>
    aliveAndComplete  &&
    ( ! unitClass.requiresPsi || powered) &&
    ! stasised        && // These three checks along comprise 6% of our CPU usage. Yes, really.
    ! maelstrommed    &&
    ! lockedDown)
  
  def canBeAttacked: Boolean = canBeAttackedCache.get
  private val canBeAttackedCache = new CacheFrame(() =>
      alive &&
      (complete || unitClass.isBuilding) &&
      totalHealth > 0 &&
      ! invincible &&
      ! stasised)
  
  def canAttack: Boolean = canAttackCache.get
  private val canAttackCache = new CacheFrame(() =>
    canDoAnything &&
    (
      unitClass.rawCanAttack
      || (is(Terran.Bunker)
      || (is(Protoss.Carrier) && interceptorCount > 0)
      || (is(Protoss.Reaver)  && scarabCount > 0)
      || (is(Zerg.Lurker)     && burrowed)
    )))
  
  def canAttack(enemy: UnitInfo): Boolean =
    canAttack                   &&
    enemy.canBeAttacked         &&
    ! enemy.effectivelyCloaked  && // Eh.
    (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround) &&
    ( ! enemy.unitClass.floats || ! unitClass.suicides || ! is(Terran.SpiderMine))
  
  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  
  def readyForAttackOrder: Boolean = canAttack && cooldownLeft <= 1 + With.latency.framesRemaining
  
  def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  def pixelReachAgainst (framesAhead: Int, enemy:UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  
  def inRangeToAttackSlow(enemy: UnitInfo)                        : Boolean = pixelsFromEdgeSlow(enemy) <= pixelRangeAgainstFromEdge(enemy)     + With.configuration.attackableRangeBuffer
  def inRangeToAttackFast(enemy: UnitInfo)                        : Boolean = pixelsFromEdgeFast(enemy) <= pixelRangeAgainstFromEdge(enemy)     + With.configuration.attackableRangeBuffer
  def inRangeToAttackSlow(enemy: UnitInfo, framesAhead  : Int)    : Boolean = enemy.project(framesAhead).pixelDistanceSlow(project(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer
  def inRangeToAttackFast(enemy: UnitInfo, framesAhead  : Int)    : Boolean = enemy.project(framesAhead).pixelDistanceFast(project(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer
  
  def framesToTravelTo(destination: Pixel)  : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  def framesToTravelPixels(pixels: Double)  : Int = if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels/topSpeed).toInt) else Int.MaxValue
  
  def framesToGetInRange(enemy: UnitInfo): Int = framesToGetInRange(enemy, enemy.pixelCenter)
  def framesToGetInRange(enemy: UnitInfo, at: Pixel): Int = if (canAttack(enemy)) framesToTravelPixels(pixelDistanceFast(at) - pixelRangeAgainstFromCenter(enemy)) else Int.MaxValue
  def framesBeforeAttacking(enemy: UnitInfo): Int = framesBeforeAttacking(enemy, enemy.pixelCenter)
  def framesBeforeAttacking(enemy: UnitInfo, at: Pixel): Int = {
    if (canAttack(enemy)) {
      Math.max(cooldownLeft, framesToGetInRange(enemy))
    }
    else Int.MaxValue
  }
  
  ////////////
  // Orders //
  ////////////
  
  def gathering: Boolean = gatheringMinerals || gatheringGas
  
  def carryingResources:Boolean = carryingMinerals || carryingGas
  
  def isBeingViolent: Boolean = {
    attacking ||
    target.orElse(orderTarget).exists(isEnemyOf) ||
    List(Commands.Attack_Move, Commands.Attack_Unit).contains(command.map(_.getUnitCommandType.toString).getOrElse(""))
  }
  
  def isBeingViolentTo(victim: UnitInfo): Boolean = {
    //Are we capable of hurting the victim?
    isEnemyOf(victim) &&
    canAttack(victim) &&
    //Are we not attacking anyone else?
    ! target.orElse(orderTarget).exists(_ != victim) &&
    //Are we close to being able to hit the victim?
    victim.pixelDistanceFast(
      pixelCenter.project(
        targetPixel
          .orElse(orderTargetPixel)
          .getOrElse(pixelCenter.radiateRadians(angleRadians, 10)),
        Math.min(pixelDistanceFast(victim), topSpeed * With.configuration.violenceFrameThreshold))) <=
      pixelRangeAgainstFromEdge(victim)
  }
  
  ////////////////
  // Visibility //
  ////////////////
  
  def likelyStillThere: Boolean =
    possiblyStillThere &&
    ( ! canMove || With.framesSince(lastSeen) < With.configuration.fogPositionDuration || is(Terran.SiegeTankUnsieged))
  
  def effectivelyCloaked: Boolean =
    (burrowed || cloaked) && (
      if (isFriendly) ! With.grids.enemyDetection.get(tileIncludingCenter)
      else            ! detected
    )
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player.isUs
  def isNeutral  : Boolean = player.isNeutral
  def isFriendly : Boolean = player.isAlly || isOurs
  def isEnemy    : Boolean = player.isEnemy
  def isEnemyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
}
