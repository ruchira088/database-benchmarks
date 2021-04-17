import sbt._

object Dependencies
{
  val ScalaVersion = "2.13.5"

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.4.1"

  lazy val jodaTime = "joda-time" % "joda-time" % "2.10.10"

  lazy val faker = "com.github.javafaker" % "javafaker" % "1.0.2"

  lazy val doobie = "org.tpolecat" %% "doobie-core" % "0.12.1"

  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % "0.12.1"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.2.19"

  lazy val h2 = "com.h2database" % "h2" % "1.4.200"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.14.1"

  lazy val flyway = "org.flywaydb" % "flyway-core" % "7.7.2"

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full

  lazy val scalaTypedHoles = "com.github.cb372" % "scala-typed-holes" % "0.1.8" cross CrossVersion.full

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.7"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}