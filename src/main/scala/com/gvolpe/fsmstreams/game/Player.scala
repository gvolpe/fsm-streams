package com.gvolpe.fsmstreams.game

case class Player(
    id: PlayerId,
    highestScore: PlayerScore,
    gems: Map[GemType, Int],
    memberSince: Timestamp
)
