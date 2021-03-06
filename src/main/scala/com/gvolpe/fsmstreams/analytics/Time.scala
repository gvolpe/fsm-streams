package com.gvolpe.fsmstreams.analytics

import java.time.Instant

import cats.effect.Sync
import com.gvolpe.fsmstreams.game.Timestamp

trait Time[F[_]] {
  def timestamp: F[Timestamp]
}

object Time {
  def apply[F[_]](implicit ev: Time[F]): Time[F] = ev

  implicit def syncInstance[F[_]: Sync]: Time[F] =
    new Time[F] {
      def timestamp: F[Timestamp] = F.delay(Timestamp(Instant.now()))
    }
}
