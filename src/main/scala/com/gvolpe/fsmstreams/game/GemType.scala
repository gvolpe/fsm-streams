package com.gvolpe.fsmstreams.game

sealed trait GemType
object GemType {
  case object Diamond  extends GemType
  case object Emerald  extends GemType
  case object Ruby     extends GemType
  case object Sapphire extends GemType
}
