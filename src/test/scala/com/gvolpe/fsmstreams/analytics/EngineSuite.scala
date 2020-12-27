package com.gvolpe.fsmstreams
package analytics

import scala.concurrent.duration._

import game._
import generators._

import cats.effect._
import cats.effect.concurrent.Ref
import fs2.Stream
import org.scalacheck.Prop._
import org.scalacheck.Gen

class EngineSuite extends IOSuite {

  override def scalaCheckTestParameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(2)

  test("Aggregate game events by player id") {
    forAll(Gen.nonEmptyListOf(genEvent)) { events =>
      ioTest {
        for {
          ref    <- Ref.of[IO, List[Summary]](List.empty)
          ticker <- Ticker.create[IO](500, 2.seconds)
          engine = Engine[IO](s => ref.update(_ :+ s), ticker)
          _      <- engine.run(Stream.emits(events)).compile.drain
          result <- ref.get
        } yield {
          val levelUpEvents      = events.collect { case e: Event.LevelUp      => e }.size
          val puzzleSolvedEvents = events.collect { case e: Event.PuzzleSolved => e }.size
          val gemCollectedEvents = events.collect { case e: Event.GemCollected => e }.size

          val score = levelUpEvents * 100 + puzzleSolvedEvents * 50 + gemCollectedEvents * 10

          val numberOfPlayers = events.groupBy(e => Event._PlayerId.headOption(e)).size

          val level = Event._LevelUp_Last_Level_Sum.fold(events)

          assertEquals(result.map(_.points).sum, score)
          assertEquals(result.size, numberOfPlayers)
          assertEquals(result.map(_.level.value).sum, level)
          assertEquals(result.map(_.gems.values.toList.sum).sum, gemCollectedEvents)
        }
      }
    }
  }

}
