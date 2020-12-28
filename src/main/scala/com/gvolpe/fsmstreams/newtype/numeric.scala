package com.gvolpe.fsmstreams.newtype

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object numeric {
  implicit class CoercibleNumOps[A: Coercible[B, *], B](lhs: A)(implicit ev: Numeric[B]) {
    def +(rhs: B): A = ev.plus(lhs.asInstanceOf[B], rhs).coerce[A]
    def -(rhs: B): A = ev.minus(lhs.asInstanceOf[B], rhs).coerce[A]
    def *(rhs: B): A = ev.times(lhs.asInstanceOf[B], rhs).coerce[A]
  }
}
