package com.ruchij.types

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import com.github.javafaker.Faker

import java.util.UUID

trait RandomGenerator[F[+ _], +A] {
  val generate: F[A]
}

object RandomGenerator {
  def apply[F[+ _], A](implicit randomGenerator: RandomGenerator[F, A]): RandomGenerator[F, A] = randomGenerator

  def create[F[+ _]: Sync, A](block: => A): RandomGenerator[F, A] =
    new RandomGenerator[F, A] {
      override val generate: F[A] = Sync[F].delay(block)
    }

  def lift[F[+ _], A](value: F[A]): RandomGenerator[F, A] =
    new RandomGenerator[F, A] {
      override val generate: F[A] = value
    }

  implicit def uuidRandomGenerator[F[+ _]: Sync]: RandomGenerator[F, UUID] = create(UUID.randomUUID())

  implicit def randomGeneratorMonad[F[+ _]: Monad]: Monad[RandomGenerator[F, *]] =
    new Monad[RandomGenerator[F, *]] {
      override def flatMap[A, B](fa: RandomGenerator[F, A])(f: A => RandomGenerator[F, B]): RandomGenerator[F, B] =
        new RandomGenerator[F, B] {
          override val generate: F[B] =
            fa.generate.flatMap(a => f(a).generate)
        }

      override def tailRecM[A, B](a: A)(f: A => RandomGenerator[F, Either[A, B]]): RandomGenerator[F, B] =
        new RandomGenerator[F, B] {
          override val generate: F[B] =
            f(a).generate.flatMap {
              case Left(value) => tailRecM(value)(f).generate

              case Right(value) => pure(value).generate
            }
        }

      override def pure[A](x: A): RandomGenerator[F, A] =
        new RandomGenerator[F, A] {
          override val generate: F[A] = Applicative[F].pure(x)
        }
    }

  val faker: Faker = Faker.instance()
}
