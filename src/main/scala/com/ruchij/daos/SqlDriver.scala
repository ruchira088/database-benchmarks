package com.ruchij.daos

import org.{h2, postgresql}

import java.sql.Driver

sealed abstract class SqlDriver[A <: Driver] {
  val dbType: String

  val clazz: Class[A]
}

object SqlDriver {
  private val DatabaseDriverRegex = "jdbc:([^:]+):.*".r

  case object Postgresql extends SqlDriver[postgresql.Driver] {
    override val dbType: String = "postgresql"

    override val clazz: Class[postgresql.Driver] = classOf[postgresql.Driver]
  }

  case object H2 extends SqlDriver[h2.Driver] {
    override val dbType: String = "h2"

    override val clazz: Class[h2.Driver] = classOf[h2.Driver]
  }

  val values: Seq[SqlDriver[_]] = Seq(Postgresql, H2)

  val inferDriver: String => Option[SqlDriver[_]] = {
    case DatabaseDriverRegex(driver) =>
      values.find(_.dbType.equalsIgnoreCase(driver))

    case _ => None
  }

}
