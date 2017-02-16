package Plans.Generic.Macro

import Plans.Plan
import Startup.With

class BuildWorkersContinuously extends AbstractBuildContinuously {
  
  override def _buildPlan:Plan = {
    //This only builds workers of our own race. Sad!
    new TrainUnit(With.game.self.getRace.getWorker)
  }
  
  override def _additionalPlansRequired:Int = {
    Math.max(0, _additionalWorkersDesired - _currentBuilds.size)
  }
  
  def _additionalWorkersDesired:Int = {
    Math.min(_workerCap - _workersNow, _maxWorkersToBuildSimultaneously)
  }
  
  def _workersNow:Int = {
    With.economist.ourActiveHarvesters.size
  }
  
  def _workerCap:Int = {
    //Cap the number of bases to saturate so we don't accidentally max out on probes
    //Assuming we want three on gas and 2.5 per mineral
    Math.min(3, With.economist.ourMiningBases.size) * (3 + 9 * 5/2)
  }
  
  def _maxWorkersToBuildSimultaneously:Int = {
    Math.max(With.economist.ourMiningBases.size, 3 * _hatcheries)
  }
  
  def _hatcheries:Int = {
    With.ourUnits.filter(_.getType.producesLarva).size
  }
}