package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT1015GateDT,
  new PvT1GateReaver,
  new PvTStove,
  new PvTBasic
)