package ProxyBwapi.Races

import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClass.UnitClasses
import ProxyBwapi.Upgrades.Upgrades
import bwapi.{TechType, UnitType, UpgradeType}

object Zerg {
  def Drone                 = UnitClasses.get(UnitType.Zerg_Drone)
  def Larva                 = UnitClasses.get(UnitType.Zerg_Larva)
  def Egg                   = UnitClasses.get(UnitType.Zerg_Egg)
  def Overlord              = UnitClasses.get(UnitType.Zerg_Overlord)
  def Zergling              = UnitClasses.get(UnitType.Zerg_Zergling)
  def Hydralisk             = UnitClasses.get(UnitType.Zerg_Hydralisk)
  def Mutalisk              = UnitClasses.get(UnitType.Zerg_Mutalisk)
  def Ultralisk             = UnitClasses.get(UnitType.Zerg_Ultralisk)
  def Scourge               = UnitClasses.get(UnitType.Zerg_Scourge)
  def Cocoon                = UnitClasses.get(UnitType.Zerg_Cocoon)
  def Guardian              = UnitClasses.get(UnitType.Zerg_Guardian)
  def Devourer              = UnitClasses.get(UnitType.Zerg_Devourer)
  def Queen                 = UnitClasses.get(UnitType.Zerg_Queen)
  def Defiler               = UnitClasses.get(UnitType.Zerg_Defiler)
  def LurkerEgg             = UnitClasses.get(UnitType.Zerg_Lurker_Egg)
  def Lurker                = UnitClasses.get(UnitType.Zerg_Lurker)
  def Broodling             = UnitClasses.get(UnitType.Zerg_Broodling)
  def InfestedTerran        = UnitClasses.get(UnitType.Zerg_Infested_Terran)
  def Hatchery              = UnitClasses.get(UnitType.Zerg_Hatchery)
  def Extractor             = UnitClasses.get(UnitType.Zerg_Extractor)
  def SpawningPool          = UnitClasses.get(UnitType.Zerg_Spawning_Pool)
  def HydraliskDen          = UnitClasses.get(UnitType.Zerg_Hydralisk_Den)
  def EvolutionChamber      = UnitClasses.get(UnitType.Zerg_Evolution_Chamber)
  def CreepColony           = UnitClasses.get(UnitType.Zerg_Creep_Colony)
  def SporeColony           = UnitClasses.get(UnitType.Zerg_Spore_Colony)
  def SunkenColony          = UnitClasses.get(UnitType.Zerg_Sunken_Colony)
  def Lair                  = UnitClasses.get(UnitType.Zerg_Lair)
  def QueensNest            = UnitClasses.get(UnitType.Zerg_Queens_Nest)
  def Spire                 = UnitClasses.get(UnitType.Zerg_Spire)
  def Hive                  = UnitClasses.get(UnitType.Zerg_Hive)
  def GreaterSpire          = UnitClasses.get(UnitType.Zerg_Greater_Spire)
  def UltraliskCavern       = UnitClasses.get(UnitType.Zerg_Ultralisk_Cavern)
  def DefilerMound          = UnitClasses.get(UnitType.Zerg_Defiler_Mound)
  def NydusCanal            = UnitClasses.get(UnitType.Zerg_Nydus_Canal)
  def InfestedCommandCenter = UnitClasses.get(UnitType.Zerg_Infested_Command_Center)
  def ZerglingAttackSpeed   = Upgrades.get(UpgradeType.Adrenal_Glands)
  def UltraliskSpeed        = Upgrades.get(UpgradeType.Anabolic_Synthesis)
  def OverlordVisionRange   = Upgrades.get(UpgradeType.Antennae)
  def UltraliskArmor        = Upgrades.get(UpgradeType.Chitinous_Plating)
  def QueenEnergy           = Upgrades.get(UpgradeType.Gamete_Meiosis)
  def HydraliskRange        = Upgrades.get(UpgradeType.Grooved_Spines)
  def ZerglingSpeed         = Upgrades.get(UpgradeType.Metabolic_Boost)
  def DefilerEnergy         = Upgrades.get(UpgradeType.Metasynaptic_Node)
  def HydraliskSpeed        = Upgrades.get(UpgradeType.Muscular_Augments)
  def OverlordSpeed         = Upgrades.get(UpgradeType.Pneumatized_Carapace)
  def OverlordDrops         = Upgrades.get(UpgradeType.Ventral_Sacs)
  def GroundArmor           = Upgrades.get(UpgradeType.Zerg_Carapace)
  def GroundRangeDamage     = Upgrades.get(UpgradeType.Zerg_Missile_Attacks)
  def GroundMeleeDamage     = Upgrades.get(UpgradeType.Zerg_Melee_Attacks)
  def AirDamage             = Upgrades.get(UpgradeType.Zerg_Flyer_Attacks)
  def AirArmor              = Upgrades.get(UpgradeType.Zerg_Flyer_Carapace)
  def Burrow                = Techs.get(TechType.Burrowing)
  def Consume               = Techs.get(TechType.Consume)
  def DarkSwarm             = Techs.get(TechType.Dark_Swarm)
  def Ensnare               = Techs.get(TechType.Ensnare)
  def InfestCommandCenter   = Techs.get(TechType.Infestation)
  def LurkerMorph           = Techs.get(TechType.Lurker_Aspect)
  def Parasite              = Techs.get(TechType.Parasite)
  def Plague                = Techs.get(TechType.Plague)
  def SpawnBroodlings       = Techs.get(TechType.Spawn_Broodlings)
}
