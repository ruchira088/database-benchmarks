package com.ruchij.daos.provider

import cats.effect.{Async, ContextShift, Resource}
import cats.implicits._
import cats.~>
import com.ruchij.daos.PersonDao
import com.ruchij.daos.provider.DaoProviderType.SqlDatabase
import pureconfig.ConfigObjectSource

trait DaoProvider[F[_]] {
  type G[A]

  val transactor: Resource[F, G ~> F]

  val personDao: F[PersonDao[G]]
}

object DaoProvider {

  def run[F[_]: Async: ContextShift](configObjectSource: ConfigObjectSource): F[DaoProvider[F]] =
    DaoProviderType.load[F](configObjectSource)
      .map {
        case SqlDatabase => new DoobieProvider[F](configObjectSource)
      }

}
