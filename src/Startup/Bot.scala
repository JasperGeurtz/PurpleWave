package Startup

import Debugging.{Visualization, _}
import Macro.Allocation._
import Micro.Battles.Battles
import ProxyBwapi.UnitAbstraction.Units
import Information.Grids.Grids
import Information.Geography.Geography
import Information._
import Macro.Scheduling.Scheduler
import Planning.Plans.GamePlans.WinTheGame
import Macro.Architect
import Micro.{Commander, Paths}
import _root_.Performance.Latency
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    try {
      With.self = With.game.self()
      With.mapWidth = With.game.mapWidth
      With.mapHeight = With.game.mapHeight
      With.configuration = new Configuration
      With.logger = new Logger
      
      With.logger.debug("Loading BWTA.")
      BWTA.readMap()
      BWTA.analyze()
      //These may not be necessary since BWTA2 doesn't seem to work in BWMirror
      BWTA.computeDistanceTransform()
      BWTA.buildChokeNodes()
      With.logger.debug("BWTA analysis complete.")
      
      With.architect    = new Architect
      With.bank         = new Banker
      With.camera       = new AutoCamera
      With.battles      = new Battles
      With.commander    = new Commander
      With.economy      = new Economy
      With.geography    = new Geography
      With.gameplan     = new WinTheGame
      With.history      = new History
      With.intelligence = new Intelligence
      With.latency      = new Latency
      With.grids        = new Grids
      With.paths        = new Paths
      With.performance  = new Performance
      With.prioritizer  = new Prioritizer
      With.recruiter    = new Recruiter
      With.scheduler    = new Scheduler
      With.units        = new Units

      With.game.enableFlag(1)
      With.game.setLocalSpeed(With.configuration.gameSpeed)
      With.game.setLatCom(With.configuration.enableLatencyCompensation)
    }
    catch { case exception:Exception =>
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onFrame() {
    try {
      With.frame = With.game.getFrameCount
      With.performance.startCounting()
      With.latency.onFrame()
      if (With.latency.shouldRun) {
        With.units.onFrame()
        With.grids.onFrame()
        With.battles.onFrame()
        With.economy.onFrame()
        With.bank.onFrame()
        With.recruiter.onFrame()
        With.prioritizer.onFrame()
        With.gameplan.onFrame()
        With.scheduler.onFrame()
        With.commander.onFrame()
      }
      With.performance.stopCounting()
      
      With.camera.onFrame()
      Visualization.Visualization.onFrame()
      considerSurrender
    }
    catch {
      case exception:Exception =>
        val dontLoseTheExceptionWhileDebugging = exception
        val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
        With.logger.onException(exception)
    }
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    try {
      With.units.onUnitDestroy(unit)
      With.history.onUnitDestroy(unit)
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  override def onEnd(isWinner: Boolean) {
    try {
      With.logger.debug(if (isWinner) "Good game! I still think you're beautiful." else "Good game! Let's pretend this never happened.")
      With.logger.flush
      BWTA.cleanMemory()
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  override def onSendText(text: String) {
    text match {
      case "c" => With.configuration.enableCamera = ! With.configuration.enableCamera
      case "f" => With.game.setLocalSpeed(10000)
      case "o" => With.configuration.enableVisualization = ! With.configuration.enableVisualization
      case "slow" => With.game.setLocalSpeed(48)
      case "fast" => With.game.setLocalSpeed(0)
    }
  }
  
  private def considerSurrender() = {
    if (With.self.supplyUsed == 0
      && With.self.minerals < 50
      && With.units.enemy.exists(_.utype.isWorker)
      && With.units.enemy.exists(_.utype.isResourceDepot)) {
      With.game.sendText("Good game! Let's pretend this never happened.")
      With.game.leaveGame()
    }
  }
}
