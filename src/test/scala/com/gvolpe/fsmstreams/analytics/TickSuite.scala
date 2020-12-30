package com.gvolpe.fsmstreams.analytics

import cats.kernel.laws.discipline.SemigroupTests
import munit.DisciplineSuite
import org.scalacheck.{Arbitrary, Gen}

class TickSuite extends DisciplineSuite {

  implicit val tickArb: Arbitrary[Tick] = Arbitrary {
    Gen.oneOf[Tick](List(Tick.On, Tick.Off))
  }

  checkAll("Semigroup[Tick]", SemigroupTests[Tick].semigroup)

}
