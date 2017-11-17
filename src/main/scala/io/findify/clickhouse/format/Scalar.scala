package io.findify.clickhouse.format

import org.joda.time.{LocalDate, LocalDateTime}

sealed trait Scalar[T]

object Scalar {
  implicit def intScalar = new Scalar[Int] {}
  implicit def longScalar = new Scalar[Long] {}
  implicit def floatScalar = new Scalar[Float] {}
  implicit def doubleScalar = new Scalar[Double] {}
  implicit def byteScalar = new Scalar[Byte] {}
  implicit def booleanScalar = new Scalar[Boolean] {}
  implicit def stringScalar = new Scalar[String] {}
  implicit def dateScalar = new Scalar[LocalDate] {}
  implicit def dateTimeScalar = new Scalar[LocalDateTime] {}
}
