package com.gvolpe.fsmstreams.game

import scala.concurrent.duration._

sealed trait Event {
  def createdAt: Timestamp
}

object Event {
  case class LevelUp(
      playerId: PlayerId,
      newLevel: Level,
      createdAt: Timestamp
  ) extends Event

  case class PuzzleSolved(
      playerId: PlayerId,
      puzzleName: PuzzleName,
      duration: FiniteDuration,
      createdAt: Timestamp
  ) extends Event

  case class GemCollected(
      playerId: PlayerId,
      gemType: GemType,
      createdAt: Timestamp
  ) extends Event
}
