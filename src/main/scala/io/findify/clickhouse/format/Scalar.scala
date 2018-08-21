package io.findify.clickhouse.format

import org.joda.time.{LocalDate, LocalDateTime}

sealed trait Scalar[T]

object Scalar {
  implicit val intScalar = new Scalar[Int] {}
  implicit val longScalar = new Scalar[Long] {}
  implicit val floatScalar = new Scalar[Float] {}
  implicit val doubleScalar = new Scalar[Double] {}
  implicit val byteScalar = new Scalar[Byte] {}
  implicit val booleanScalar = new Scalar[Boolean] {}
  implicit val stringScalar = new Scalar[String] {}
  implicit val dateScalar = new Scalar[LocalDate] {}
  implicit val dateTimeScalar = new Scalar[LocalDateTime] {}
}
