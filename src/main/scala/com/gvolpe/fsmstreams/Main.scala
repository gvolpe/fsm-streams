package com.gvolpe.fsmstreams

import scala.concurrent.duration._

import java.util.UUID

import com.gvolpe.fsmstreams.analytics._
import com.gvolpe.fsmstreams.game._

import cats.effect._
import cats.syntax.all._
import fs2.Stream

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)

  val program =
    Stream
      .eval((Time[IO].timestamp, IO(PlayerId(UUID.randomUUID())), Ticker.create[IO](500, 2.seconds)).tupled)
      .flatMap {
        case (ts, pid, ticker) =>
          Engine(s => IO(println(s)), ticker).run {
            Stream(
              Event.LevelUp(pid, Level(5), ts),
              Event.GemCollected(pid, GemType.Diamond, ts),
              Event.PuzzleSolved(pid, PuzzleName("Gold"), 10.seconds, ts),
              Event.GemCollected(pid, GemType.Sapphire, ts)
            )
          }
      }

}
