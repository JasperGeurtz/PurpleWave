package ProxyBwapi.Engine

import bwapi.UnitSizeType

object Size {
  trait Type
  object Small  extends Type
  object Medium extends Type
  object Large  extends Type

  def get(t: UnitSizeType): Type = {
    if (t == UnitSizeType.Small) Small
    else if (t == UnitSizeType.Medium) Medium
    else Large
  }
}
