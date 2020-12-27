package com.gvolpe.fsmstreams.analytics

case class FSM[F[_], S, I, O](run: (S, I) => F[(S, O)])
