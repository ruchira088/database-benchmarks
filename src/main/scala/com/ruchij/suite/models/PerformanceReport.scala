package com.ruchij.suite.models

import scala.concurrent.duration.FiniteDuration

case class PerformanceReport(
  insertions: FiniteDuration,
  findById: FiniteDuration,
  findByUsername: FiniteDuration,
  count: Long
)
