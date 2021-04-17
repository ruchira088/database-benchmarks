package com.ruchij.daos

import cats.ApplicativeError
import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import com.ruchij.config.SqlDatabaseConfiguration
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

object DoobieTransactor {

  def create[F[_]: Async: ContextShift](
    sqlDatabaseConfiguration: SqlDatabaseConfiguration
  ): Resource[F, HikariTransactor[F]] =
    Resource
      .eval(migrate[F](sqlDatabaseConfiguration))
      .productR {
        SqlDriver
          .inferDriver(sqlDatabaseConfiguration.url)
          .fold[Resource[F, HikariTransactor[F]]](Resource.eval {
            ApplicativeError[F, Throwable].raiseError {
              new IllegalArgumentException(s"Unable to infer database driver from ${sqlDatabaseConfiguration.url}")
            }
          }) { sqlDriver =>
            for {
              connectEC <- ExecutionContexts.fixedThreadPool(16)

              blocker <- Blocker[F]

              transactor <- HikariTransactor.newHikariTransactor(
                sqlDriver.clazz.getName,
                sqlDatabaseConfiguration.url,
                sqlDatabaseConfiguration.user,
                sqlDatabaseConfiguration.password,
                connectEC,
                blocker
              )
            } yield transactor
          }
      }

  def migrate[F[_]: Sync](sqlDatabaseConfiguration: SqlDatabaseConfiguration): F[MigrateResult] =
    for {
      flyway <- Sync[F].delay {
        Flyway
          .configure()
          .dataSource(sqlDatabaseConfiguration.url, sqlDatabaseConfiguration.user, sqlDatabaseConfiguration.password)
          .load()
      }

      migrationResult <- Sync[F].delay(flyway.migrate())

    } yield migrationResult

}
