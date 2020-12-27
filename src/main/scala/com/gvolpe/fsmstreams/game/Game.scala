package com.gvolpe.fsmstreams.game

case class Game(
    playerId: PlayerId,
    playerScore: PlayerScore,
    level: Level,
    gems: Map[GemType, Int]
)
