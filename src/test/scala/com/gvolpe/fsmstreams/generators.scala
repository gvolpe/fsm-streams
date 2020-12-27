package com.gvolpe.fsmstreams

import java.time.Instant
import java.util.UUID

import game._

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.scalacheck.Gen

object generators {

  val genNonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  def cbUUID[A: Coercible[UUID, *]]: Gen[A] =
    Gen.uuid.map(_.coerce[A])

  def cbInt[A: Coercible[Int, *]]: Gen[A] =
    Gen.posNum[Int].map(_.coerce[A])

  def cbStr[A: Coercible[String, *]]: Gen[A] =
    genNonEmptyString.map(_.coerce[A])

  val genTimestamp: Gen[Timestamp] =
    Gen
      .choose[Long](
        Instant.parse("1980-01-01T00:00:00.000Z").getEpochSecond(),
        Instant.parse("3000-01-01T00:00:00.000Z").getEpochSecond()
      )
      .map(millis => Timestamp(Instant.ofEpochMilli(millis)))

  val uuidPool = List.fill(2)(UUID.randomUUID())

  val genPlayerIdFromPool: Gen[PlayerId] =
    Gen.oneOf(uuidPool.map(PlayerId.apply))

  val genLevelUp: Gen[Event.LevelUp] =
    for {
      id <- genPlayerIdFromPool
      lv <- cbInt[Level]
      ts <- genTimestamp
    } yield Event.LevelUp(id, lv, ts)

  val genPuzzleSolved: Gen[Event.PuzzleSolved] =
    for {
      id <- genPlayerIdFromPool
      pn <- cbStr[PuzzleName]
      fd <- Gen.finiteDuration
      ts <- genTimestamp
    } yield Event.PuzzleSolved(id, pn, fd, ts)

  val genGemType: Gen[GemType] =
    Gen.oneOf[GemType](
      List(GemType.Diamond, GemType.Emerald, GemType.Ruby, GemType.Sapphire)
    )

  val genGemCollected: Gen[Event.GemCollected] =
    for {
      id <- genPlayerIdFromPool
      gt <- genGemType
      ts <- genTimestamp
    } yield Event.GemCollected(id, gt, ts)

  val genEvent: Gen[Event] =
    Gen.oneOf(genLevelUp, genPuzzleSolved, genGemCollected)

}
