package com.ruchij.suite

import cats.effect.{Clock, Concurrent, Sync}
import cats.implicits._
import cats.{Applicative, ApplicativeError, ~>}
import com.ruchij.daos.PersonDao
import com.ruchij.models.Person
import com.ruchij.suite.models.PerformanceReport
import com.ruchij.types.{JodaClock, RandomGenerator}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object PerformanceTest {

  def run[F[+ _]: Clock: Sync: Concurrent, G[_]](concurrency: Int, personDao: PersonDao[G])(implicit transaction: G ~> F): F[PerformanceReport] =
    for {
      people <- List.range(0, concurrency).traverse(_ => RandomGenerator[F, Person].generate)

      timestampA <- JodaClock[F].timestamp

      _ <- people
        .traverse { person =>
          Concurrent[F].start(transaction(personDao.insert(person)))
        }
        .flatMap(_.traverse(_.join))

      timestampB <- JodaClock[F].timestamp

      findById <- people
        .traverse { person =>
          Concurrent[F].start {
            transaction(personDao.findById(person.id))
              .flatMap {
                _.fold[F[Person]](
                  ApplicativeError[F, Throwable].raiseError(new Exception(s"Unable to find userId = ${person.id}"))
                ) { person =>
                  Applicative[F].pure(person)
                }
              }
          }
        }
        .flatMap(_.traverse(_.join))

      _ <- findById.mustMatch[F](people)

      timestampC <- JodaClock[F].timestamp

      findByUsername <- people
        .traverse { person =>
          Concurrent[F].start {
            transaction(personDao.findByUsername(person.username))
              .flatMap {
                _.fold[F[Person]](
                  ApplicativeError[F, Throwable]
                    .raiseError(new Exception(s"Unable to find username = ${person.username}"))
                ) { person =>
                  Applicative[F].pure(person)
                }
              }
          }
        }
        .flatMap(_.traverse(_.join))

      _ <- findByUsername.mustMatch[F](people)

      timestampD <- JodaClock[F].timestamp

      count <- transaction(personDao.count)

    } yield
      PerformanceReport(
        FiniteDuration(timestampB.getMillis - timestampA.getMillis, TimeUnit.MILLISECONDS),
        FiniteDuration(timestampC.getMillis - timestampB.getMillis, TimeUnit.MILLISECONDS),
        FiniteDuration(timestampD.getMillis - timestampC.getMillis, TimeUnit.MILLISECONDS),
        count
      )

  implicit class MatcherF[A](result: A) {
    def mustMatch[F[_]: ApplicativeError[*[_], Throwable]](expected: A): F[Unit] =
      if (result == expected) Applicative[F].unit
      else ApplicativeError[F, Throwable].raiseError(new Exception(s"Expected: $expected, Actual: $result"))
  }

}
