package com.gvolpe.fsmstreams

import java.time.Instant

import scala.concurrent.ExecutionContext

import com.gvolpe.fsmstreams.analytics.Time
import com.gvolpe.fsmstreams.game.Timestamp

import cats.effect._
import munit._

trait IOSpec extends ScalaCheckSuite {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)

  implicit val time: Time[IO] = new Time[IO] {
    def timestamp: IO[Timestamp] = IO(Timestamp(Instant.parse("2020-12-03T10:15:30.00Z")))
  }

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms :+ new ValueTransform("IO", {
      case ioa: IO[_] => IO.suspend(ioa).unsafeToFuture()
    })

  def assertIO(b: Boolean)(implicit loc: Location): IO[Unit] = IO(assert(b))
}
