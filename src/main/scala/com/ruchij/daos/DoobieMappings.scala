package com.ruchij.daos

import doobie.implicits.javasql.TimestampMeta
import doobie.util.{Get, Put}
import org.joda.time.DateTime

import java.sql.Timestamp
import java.util.UUID
import scala.util.Try

object DoobieMappings {

  implicit val dateTimePut: Put[DateTime] = Put[Timestamp].contramap[DateTime](dateTime => new Timestamp(dateTime.getMillis))

  implicit val dateTimeGet: Get[DateTime] = Get[Timestamp].map(date => new DateTime(date.getTime))

  implicit val uuidPut: Put[UUID] = Put[String].contramap[UUID](_.toString)

  implicit val uuidGet: Get[UUID] =
    Get[String].temap(value => Try(UUID.fromString(value)).toEither.left.map(_.getMessage))

}
