package com.gvolpe.fsmstreams.analytics

import com.gvolpe.fsmstreams.analytics.Ticker.Count
import com.gvolpe.fsmstreams.game._
import com.gvolpe.fsmstreams.newtype.numeric._

import cats._
import cats.effect._
import cats.syntax.all._
import fs2._

case class Engine[F[_]: Concurrent: Parallel: Time: Timer](
    publish: Summary => F[Unit],
    ticker: Ticker[F]
) {
  private val fsm = Engine.fsm[F](ticker)

  def run: Pipe[F, Event, Unit] =
    _.noneTerminate
      .zip(ticker.ticks)
      .evalMapAccumulate(Map.empty[PlayerId, Agg] -> 0)(fsm.run)
      .collect { case (_, (out, Tick.On)) => out }
      .evalMap { m =>
        F.timestamp.flatMap { ts =>
          m.toList.parTraverse_ {
            case (pid, agg) => publish(agg.summary(pid, ts))
          }
        }
      }
}

object Engine {
  type Result = (Map[PlayerId, Agg], Tick)
  type State  = (Map[PlayerId, Agg], Count)

  def fsm[F[_]: Applicative](
      ticker: Ticker[F]
  ): FSM[F, State, (Option[Event], Tick), Result] = {
    def go(
        m: Map[PlayerId, Agg],
        count: Count,
        playerId: PlayerId,
        tick: Tick,
        f: Agg => Agg
    ): F[(State, Result)] = {
      val agg = m.getOrElse(playerId, Agg.empty)
      val out = m.updated(playerId, f(agg))
      val nst = if (tick === Tick.On) Map.empty[PlayerId, Agg] else out

      ticker.merge(tick, count).map {
        case (newTick, newCount) =>
          (nst -> newCount) -> (out -> newTick)
      }
    }

    FSM {
      case ((m, count), (Some(event), tick)) =>
        val (playerId, modifier) =
          event match {
            case Event.LevelUp(pid, level, _) =>
              pid -> Agg._Points
                .modify(_ + 100)
                .andThen(Agg._Level.set(level))
            case Event.PuzzleSolved(pid, _, _, _) =>
              pid -> Agg._Points.modify(_ + 50)
            case Event.GemCollected(pid, gemType, _) =>
              pid -> Agg._Points
                .modify(_ + 10)
                .andThen(Agg._Gems.modify(_.updatedWith(gemType)(_.map(_ + 1).orElse(Some(1)))))
          }
        go(m, count, playerId, tick, modifier)
      case ((m, _), (None, _)) =>
        F.pure((Map.empty -> 0) -> (m -> Tick.On))
    }
  }

}
