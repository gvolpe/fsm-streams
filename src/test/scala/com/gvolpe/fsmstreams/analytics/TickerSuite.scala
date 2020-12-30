package com.gvolpe.fsmstreams
package analytics

import scala.concurrent.duration._

import cats.effect._
import cats.effect.concurrent._
import cats.syntax.all._

class TickerSuite extends IOSuite {

  private var clockValue: Long = System.nanoTime()

  private def tick(d: FiniteDuration): IO[Unit] = IO {
    clockValue = clockValue + d.toNanos
  }

  implicit val testClock: Clock[IO] =
    new Clock[IO] {
      def monotonic(unit: TimeUnit): IO[Long] = IO.pure(clockValue)
      def realTime(unit: TimeUnit): IO[Long]  = IO.pure(clockValue)
    }

  test("Ticker emits timer ticks every specified time") {
    for {
      ticker  <- Ticker.create[IO](10, 2.seconds)
      gate    <- Deferred[IO, Either[Throwable, Unit]]
      counter <- Ref.of[IO, Int](0)
      syncer  <- Deferred[IO, Unit]
      syncAttempt = counter.updateAndGet(_ + 1).flatMap(n => syncer.complete(()).attempt.void.whenA(n > 20))
      counterTick <- ticker.get
      fb          <- ticker.ticks.interruptWhen(gate).evalTap(_ => syncAttempt).compile.toList.start
      _           <- tick(2.seconds.plus(50.millis))
      _           <- syncer.get // let the ticks run
      _           <- gate.complete(Right(()))
      timerTicks  <- fb.join
    } yield {
      assertEquals(counterTick, Tick.Off)
      assertEquals(timerTicks.count(_ === Tick.On), 1)
    }
  }

  test("Ticker emits counter ticks every specified number of events") {
    for {
      ticker  <- Ticker.create[IO](10, 2.seconds)
      gate    <- Deferred[IO, Either[Throwable, Unit]]
      counter <- Ref.of[IO, Int](0)
      syncer  <- Deferred[IO, Unit]
      syncAttempt = counter.updateAndGet(_ + 1).flatMap(n => syncer.complete(()).attempt.void.whenA(n > 20))
      counterTick1 <- ticker.get
      fb           <- ticker.ticks.interruptWhen(gate).evalTap(_ => syncAttempt).compile.toList.start
      _            <- tick(2.seconds.plus(10.millis))
      _            <- syncer.get // let the ticks run
      _            <- ticker.merge(Tick.Off, 10)
      _            <- gate.complete(Right(()))
      allTicks     <- fb.join
      counterTick2 <- ticker.get
    } yield {
      assertEquals(counterTick1, Tick.Off)
      assertEquals(counterTick2, Tick.On)
      assertEquals(allTicks.count(_ === Tick.On), 1)
    }
  }

}
