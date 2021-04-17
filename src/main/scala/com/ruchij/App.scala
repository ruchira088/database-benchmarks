package com.ruchij

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.~>
import com.ruchij.daos.provider.DaoProvider
import com.ruchij.suite.PerformanceTest
import pureconfig.ConfigSource

object App extends IOApp
{
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)

      daoProvider <- DaoProvider.run[IO](configObjectSource)
      personDao <- daoProvider.personDao

      _ <-
        daoProvider.transactor.use {
          implicit transactor: daoProvider.G ~> IO =>
            List.range(0, 1000_000).traverse { _ =>
              PerformanceTest.run[IO, daoProvider.G](1000, personDao)
                .flatMap { performanceReport => IO.delay(println(performanceReport)) }
            }
        }
    }
    yield ExitCode.Success
}
