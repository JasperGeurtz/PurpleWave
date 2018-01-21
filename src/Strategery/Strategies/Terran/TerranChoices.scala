package Strategery.Strategies.Terran

import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Terran.FFA.TerranFFABio
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ._
import Strategery.Strategies._

object TerranChoices {
  
  /////////
  // TvR //
  /////////
  
  val tvr = Vector(
    WorkerRush,
    TvEProxy5Rax,
    TvEProxy8Fact,
    TvEProxyBBS,
    TvEMassMarine,
    TvEMassVulture,
    TvR1Rax,
    TvRTinfoil,
    TerranFFABio)
  
  /////////
  // TvT //
  /////////
  
  val tvtOpeners = Vector(
    TvTStandard,
    TvTPNukeDrop
  )
  
  /////////
  // TvP //
  /////////
  
  val tvpOpeners = Vector(
    TvTPNukeDrop
  )
  
  /////////
  // TvZ //
  /////////
  
  val tvzOpeners = Vector(
    TvZEarlyCCFirst,
    TvZEarly1RaxGas,
    TvZEarly1RaxFEEconomic,
    TvZEarly1RaxFEConservative,
    TvZEarly2Rax
  )
  
  val normalOpeners = tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners
  
  /////////
  // All //
  /////////
  
  val all: Vector[Strategy] = (gimmickOpeners ++ normalOpeners).distinct
}