package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  // Openings
  new PvZ4Gate99,
  new PvZ4Gate,
  new PvZFFE,
  // Midgames
  new PvZ4Gate2Archon,
  new PvZBisu,
  new PvZNeoBisu,
  new PvZ5GateGoon,
  // Late game
  new PvZLateGame
)