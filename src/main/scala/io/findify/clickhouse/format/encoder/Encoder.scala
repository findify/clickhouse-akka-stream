package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field

import scala.annotation.implicitNotFound

//@implicitNotFound("Cannot derive typeclass for Encoder[${T},${F}]")
trait Encoder[T, +F <: Field] {
  def encode(name: String, value: T): Map[String, F]
}
