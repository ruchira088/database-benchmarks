package com.ruchij.models

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.ruchij.types.RandomGenerator
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

case class Person(id: UUID, createdAt: DateTime, username: String, firstName: String, lastName: String, age: Int) {
  override def equals(obj: Any): Boolean =
    obj match {
      case Person(`id`, dateTime, `username`, `firstName`, `lastName`, `age`) =>
        dateTime.getMillis == createdAt.getMillis

      case _ => false
    }
}

object Person {
  import com.ruchij.types.RandomGenerator.faker._

  implicit def randomPersonGenerator[F[+ _]: Sync: Clock]: RandomGenerator[F, Person] =
    for {
      firstName <- RandomGenerator.create[F, String](name().firstName())
      lastName <- RandomGenerator.create[F, String](name().lastName())

      suffixA <- RandomGenerator.create[F, Int](number().numberBetween(0, Int.MaxValue))
      suffixB <- RandomGenerator.create[F, Int](number().numberBetween(0, Int.MaxValue))

      username =
        s"$firstName.$lastName$suffixA.$suffixB".toLowerCase
          .filter(character => character.isLetterOrDigit || character == '.')

      id <- RandomGenerator[F, UUID]

      age <- RandomGenerator.create[F, Int](number().numberBetween(0, 120))

      timestamp <-
        RandomGenerator.lift {
          Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))
        }
    }
    yield Person(id, timestamp, username, firstName, lastName, age)
}