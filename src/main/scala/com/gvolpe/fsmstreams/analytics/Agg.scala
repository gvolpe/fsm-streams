package com.gvolpe.fsmstreams.analytics

import com.gvolpe.fsmstreams.game._

import monocle.macros._

case class Agg(
    level: Level,
    points: Points,
    gems: Map[GemType, Int]
) {
  def summary(pid: PlayerId, ts: Timestamp): Summary =
    Summary(pid, level, points, gems, ts)
}

object Agg {
  def empty = Agg(Level(0), Points(0), Map.empty)

  val _Gems   = GenLens[Agg](_.gems)
  val _Level  = GenLens[Agg](_.level)
  val _Points = GenLens[Agg](_.points)
}

case class Summary(
    playerId: PlayerId,
    level: Level,
    points: Points,
    gems: Map[GemType, Int],
    createdAt: Timestamp
)
