package com.ruchij.types

import cats.Functor
import cats.effect.Clock
import cats.implicits._
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit

trait JodaClock[F[_]] {
  val timestamp: F[DateTime]
}

object JodaClock {
  def apply[F[_]](implicit jodaClock: JodaClock[F]): JodaClock[F] = jodaClock

  implicit def fromClock[F[_]: Clock: Functor]: JodaClock[F] =
    new JodaClock[F] {
      override val timestamp: F[DateTime] =
        Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))
    }
}
