package com.gvolpe.fsmstreams
package analytics

import scala.annotation.tailrec

import game._
import generators._
import Ticker.Count

import cats.Id
import fs2.Stream
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop._

class FSMSuite extends ScalaCheckSuite {

  private val offTicker: Ticker[Id] = new Ticker[Id] {
    def get: Id[Tick]                                           = Tick.Off
    def merge(timerTick: Tick, count: Count): Id[(Tick, Count)] = timerTick -> (count + 1)
    def ticks: Stream[Id, Tick]                                 = Stream.emit(Tick.Off)
  }

  private def gems(res: Map[PlayerId, Agg]): Int   = res.values.toList.flatMap(_.gems.values.toList).sum
  private def level(res: Map[PlayerId, Agg]): Int  = res.values.toList.map(_.level.value).sum
  private def points(res: Map[PlayerId, Agg]): Int = res.values.toList.map(_.points.value).sum

  private val fsm = Engine.fsm[Id](offTicker)

  override def scalaCheckTestParameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(2)

  test("FSM specification") {
    forAll(genGemCollected, genPuzzleSolved, genLevelUp) { (e1, e2, e3) =>
      val (st1 @ (res1, count1), (out1, tick1)) = fsm.run((Map.empty -> 0), (Some(e1) -> Tick.Off))
      assertEquals(count1, 1)
      assertEquals(tick1, Tick.Off)
      assertEquals(out1, res1)
      assertEquals(gems(res1), 1)
      assertEquals(level(res1), 0)
      assertEquals(points(res1), 10)

      val (st2 @ (res2, count2), (out2, tick2)) = fsm.run(st1, (Some(e2) -> Tick.Off))
      assertEquals(count2, 2)
      assertEquals(tick2, Tick.Off)
      assertEquals(out2, res2)
      assertEquals(gems(res2), 1)
      assertEquals(level(res2), 0)
      assertEquals(points(res2), 60)

      val (st3 @ (res3, count3), (out3, tick3)) = fsm.run(st2, (Some(e3) -> Tick.Off))
      assertEquals(count3, 3)
      assertEquals(tick3, Tick.Off)
      assertEquals(out3, res3)
      assertEquals(gems(res3), 1)
      assertEquals(level(res3), e3.newLevel.value)
      assertEquals(points(res3), 160)

      // at the end of the stream, both the counter and the tick timer are reseted
      val (st4 @ (res4, count4), (out4, tick4)) = fsm.run(st3, (None -> Tick.Off))
      assertEquals(count4, 0)
      assertEquals(tick4, Tick.On)
      assert(res4.isEmpty)
      assertEquals(gems(out4), 1)
      assertEquals(level(res4), 0)
      assertEquals(points(out4), 160)

      val ((res5, count5), (out5, tick5)) = fsm.run(st4, (Some(e1) -> Tick.Off))
      assertEquals(count5, 1)
      assertEquals(tick5, Tick.Off)
      assertEquals(out5, res5)
      assertEquals(gems(res5), 1)
      assertEquals(level(res5), 0)
      assertEquals(points(res5), 10)
    }
  }

  private def baseSpec[A <: Event](
      description: String,
      generator: Gen[A],
      gemsAssertion: List[A] => Int,
      levelAssertion: List[A] => Int,
      pointsAssertion: List[A] => Int
  ): Unit =
    test(description) {
      forAll(Gen.listOf(generator)) {
        case Nil => ()
        case events @ (x :: xs) =>
          @tailrec
          def go(e: A, st: Engine.State, ys: List[A]): Engine.State = {
            val (state, (_, _)) = fsm.run(st, (Some(e) -> Tick.Off))
            ys match {
              case Nil        => state
              case (e1 :: es) => go(e1, state, es)
            }
          }

          val (res, count) = go(x, Map.empty -> 0, xs)
          assertEquals(count, events.size)
          assertEquals(gems(res), gemsAssertion(events))
          assertEquals(level(res), levelAssertion(events))
          assertEquals(points(res), pointsAssertion(events))
      }
    }

  baseSpec[Event.GemCollected](
    description = "FSM collected gems",
    generator = genGemCollected,
    gemsAssertion = _.size,
    levelAssertion = _ => 0,
    pointsAssertion = _.size * 10
  )

  baseSpec[Event.PuzzleSolved](
    description = "FSM solved puzzles",
    generator = genPuzzleSolved,
    gemsAssertion = _ => 0,
    levelAssertion = _ => 0,
    pointsAssertion = _.size * 50
  )

  baseSpec[Event.LevelUp](
    description = "FSM level up",
    generator = genLevelUp,
    gemsAssertion = _ => 0,
    levelAssertion = Event._LevelUp_Last_Level_Sum.fold(_),
    pointsAssertion = _.size * 100
  )

}
