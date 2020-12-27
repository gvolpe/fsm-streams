package com.gvolpe.fsmstreams.analytics

import com.gvolpe.fsmstreams.game._

import monocle.macros._

case class Agg(
    level: Level,
    points: Int,
    gems: Map[GemType, Int]
) {
  def summary(pid: PlayerId, ts: Timestamp): Summary =
    Summary(pid, level, points, gems, ts)
}

object Agg {
  def empty = Agg(level = Level(0), points = 0, gems = Map.empty)

  val _Gems   = GenLens[Agg](_.gems)
  val _Level  = GenLens[Agg](_.level)
  val _Points = GenLens[Agg](_.points)
}

case class Summary(
    playerId: PlayerId,
    level: Level,
    points: Int,
    gems: Map[GemType, Int],
    createdAt: Timestamp
)
