package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT25BaseCarrier,
  new PvT2GateExpandCarrier,
  new PvT1015Expand,
  new PvT1015DT,
  new PvTStove,
  new PvTBasic
)