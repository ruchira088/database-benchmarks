package com.ruchij.daos.provider

import cats.effect.{Async, ContextShift, Resource, Sync}
import cats.{Applicative, ApplicativeError, ~>}
import com.ruchij.config.SqlDatabaseConfiguration
import com.ruchij.daos.{DoobiePersonDao, DoobieTransactor, PersonDao}
import doobie.ConnectionIO
import doobie.implicits._
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

class DoobieProvider[F[_]: Async: ContextShift](configObjectSource: ConfigObjectSource) extends DaoProvider[F] {
  override type G[A] = ConnectionIO[A]

  override val transactor: Resource[F, G ~> F] =
    for {
      sqlDatabaseConfiguration <-
        Resource.eval {
          configObjectSource.at("sql-database-configuration")
            .load[SqlDatabaseConfiguration]
            .fold[F[SqlDatabaseConfiguration]](
              error => ApplicativeError[F, Throwable].raiseError(ConfigReaderException(error)),
              config => Applicative[F].pure(config)
            )
        }

      transactor <- DoobieTransactor.create[F](sqlDatabaseConfiguration)

      transaction = new ~>[ConnectionIO, F] {
        override def apply[A](connectionIO: ConnectionIO[A]): F[A] =
          Sync[F].defer(connectionIO.transact(transactor))
      }
    } yield transaction

  override val personDao: F[PersonDao[ConnectionIO]] =
    Applicative[F].pure(DoobiePersonDao)

}
