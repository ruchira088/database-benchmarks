package com.ruchij.daos.provider

import cats.{Applicative, ApplicativeError}
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException

import scala.util.Try

sealed trait DaoProviderType {
  val name: String
}

object DaoProviderType {
  case object SqlDatabase extends DaoProviderType {
    override val name: String = "sql"
  }

  val values: Seq[DaoProviderType] = Seq(SqlDatabase)

  def load[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[DaoProviderType] =
    configObjectSource
      .config()
      .map(_.resolve())
      .fold[F[DaoProviderType]](
        errors => ApplicativeError[F, Throwable].raiseError(ConfigReaderException(errors)),
        config =>
          Try(config.getString("dao-provider")).toEither
            .fold(
              throwable => ApplicativeError[F, Throwable].raiseError(throwable),
              value =>
                DaoProviderType.values
                  .find(_.name.equalsIgnoreCase(value))
                  .fold[F[DaoProviderType]](
                    ApplicativeError[F, Throwable].raiseError(
                      new IllegalArgumentException(
                        s""""$value" is not valid value. Possible values are ${DaoProviderType.values
                          .map(_.name)
                          .mkString("[", ", ", "]")}"""
                      )
                    )
                  ) { daoProviderType =>
                    Applicative[F].pure(daoProviderType)
                }
          )
      )
}
