package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Standard.PvP._

class ProtossVsProtoss extends ModalGameplan(
  new PvPOpen910Gates,
  new PvPOpen1012Gates,
  new PvPOpen1015Gates,
  new PvPOpenZZCore,
  new PvPOpenZCoreZ,
  new PvPOpenCoreZ,
  new PvPOpenGoonFirst,
  
  new PvPOneBaseGoonExpand,
  new PvPOneBaseReaverExpand,
  
  new PvPOpen1015GateGoonExpand,
  new PvPOpen1015GateGoonReaverExpand,
  new PvPOpen1015GateGoonDTs,
  new PvPOpen12Nexus5Zealot,
  new PvPOpen2GateRobo,
  new PvPOpen2GateDarkTemplar,
  new PvPOpen3GateSpeedlots,
  new PvPOpen4GateGoon,
  
  new PvPLateGame
)