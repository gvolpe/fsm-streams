package com.gvolpe.fsmstreams

import java.time.Instant
import java.util.UUID

import io.estatico.newtype.macros.newtype

package object game {
  @newtype case class Level(value: Int)
  @newtype case class Points(value: Int)
  @newtype case class PlayerId(value: UUID)
  @newtype case class PlayerScore(value: Int)
  @newtype case class PuzzleName(value: String)
  @newtype case class Timestamp(value: Instant)
}
