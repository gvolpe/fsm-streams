package com.gvolpe.fsmstreams.game

import scala.concurrent.duration._

import cats.Monoid
import monocle.Fold
import monocle.macros._

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

  val __GemCollected = GenPrism[Event, GemCollected]
  val __LevelUp      = GenPrism[Event, LevelUp]
  val __PuzzleSolved = GenPrism[Event, PuzzleSolved]

  val _PlayerId: Fold[Event, PlayerId] =
    new Fold[Event, PlayerId] {
      def foldMap[M: Monoid](f: PlayerId => M)(s: Event): M =
        s match {
          case Event.LevelUp(pid, _, _)         => f(pid)
          case Event.PuzzleSolved(pid, _, _, _) => f(pid)
          case Event.GemCollected(pid, _, _)    => f(pid)
          case _                                => M.empty
        }
    }

  val _LevelUp_Last_Level_Sum: Fold[List[Event], Int] =
    new Fold[List[Event], Int] {
      def foldMap[M: Monoid](f: Int => M)(s: List[Event]): M =
        f {
          s.flatMap(__LevelUp.getOption(_).toList)
            .groupBy[PlayerId](_.playerId)
            .map {
              case (_, xs) => xs.map(_.newLevel.value).takeRight(1).headOption.getOrElse(0)
            }
            .sum
        }
    }

}
