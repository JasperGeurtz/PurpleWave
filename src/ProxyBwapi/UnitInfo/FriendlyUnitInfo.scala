package ProxyBwapi.UnitInfo

import Information.Grids.Combat.AbstractGridEnemyRange
import Lifecycle.With
import Micro.Agency.Agent
import Micro.Squads.Squad
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

import scala.collection.JavaConverters._

class FriendlyUnitInfo(base: bwapi.Unit, id: Int) extends FriendlyUnitProxy(base, id) {
  
  override val friendly: Option[FriendlyUnitInfo] = Some(this)
  
  def squad: Option[Squad] = With.squads.squadByUnit.get(this)
  def squadmates: Set[FriendlyUnitInfo] = squad.map(_.units).getOrElse(Set.empty)
  def squadenemies: Seq[UnitInfo] = squad.map(_.enemies).getOrElse(Seq.empty)
  def readyForMicro: Boolean = With.commander.ready(this)
  def teammates: Set[UnitInfo] = teammatesCache()
  def enemies: Seq[UnitInfo] = enemiesCache()
  lazy val agent: Agent = new Agent(this)

  private val teammatesCache = new Cache(() => (squadmates ++ matchups.allies))
  private val enemiesCache = new Cache(() => (squadenemies ++ matchups.enemies).distinct)
  
  override def updateCommon() {
    super.updateCommon()
    knownToEnemyOnce = knownToEnemyOnce || visibleToOpponents
  }
  
  ////////////
  // Orders //
  ////////////
  
  var lastSetRally: Int = 0
  
  def buildUnit     : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)
  
  ////////////////
  // Visibility //
  ////////////////
  
  private var knownToEnemyOnce: Boolean = false
  def knownToEnemy: Boolean = knownToEnemyOnce
  
  //////////////
  // Statuses //
  //////////////

  def completeOrNearlyComplete: Boolean = complete || remainingCompletionFrames < With.reaction.planningMax

  private var _trainerPlan: Option[Plan] = None
  def trainerPlan: Option[Plan] = _trainerPlan
  def setTrainerPlan(myTrainer: Plan): Unit = {
    if (trainerPlan.isEmpty) {
      _trainerPlan = Some(myTrainer)
    }
  }

  def trainee: Option[FriendlyUnitInfo] = traineeCache()
  private val traineeCache = new Cache[Option[FriendlyUnitInfo]](() =>
    if (training)
      With.units.ours.find(u =>
        ! u.complete
        && u.alive
        && u.pixelCenter == pixelCenter
        && is(u.unitClass.whatBuilds._1))
    else None)
  
  def loadedUnits: Vector[FriendlyUnitInfo] = loadedUnitsCache()
  private val loadedUnitsCache = new Cache(() =>
    if (unitClass.canLoadUnits)
      base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector
    else
      Vector.empty)
  
  def transport: Option[FriendlyUnitInfo] = transportCache()
  private val transportCache = new Cache(() =>
    if (unitClass.isBuilding)
      None
    else
      With.units.get(base.getTransport).flatMap(_.friendly))
  
  def canTransport(passenger: FriendlyUnitInfo): Boolean =
    isTransport                           &&
    passenger.unitClass.canBeTransported  &&
    passenger.canMove                     &&
    passenger.transport.forall(_ == this) &&
    loadedUnits.view.filterNot(_ == passenger).map(_.unitClass.spaceRequired).sum <= unitClass.spaceProvided

  // More accurate, but avoiding for performance reasons
  // override def subjectiveValue: Double = super.subjectiveValue + trainee.map(_.subjectiveValue).sum

  def enemyRangeGrid: AbstractGridEnemyRange =
    if (flying || transport.exists(_.flying))
      With.grids.enemyRangeAir
    else
      With.grids.enemyRangeGround
}
