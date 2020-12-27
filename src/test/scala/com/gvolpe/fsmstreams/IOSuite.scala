package com.gvolpe.fsmstreams

import scala.concurrent.ExecutionContext

import cats.effect._
import munit._

trait IOSuite extends ScalaCheckSuite {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms :+ new ValueTransform("IO", {
      case ioa: IO[_] => IO.suspend(ioa).unsafeToFuture()
    })

  def ioTest[A](ioa: IO[A]): A = ioa.unsafeRunSync()

  def assertIO(b: Boolean)(implicit loc: Location): IO[Unit] = IO(assert(b))
}
