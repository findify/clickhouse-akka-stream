package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field

import scala.annotation.implicitNotFound

trait Encoder[T, +F <: Field] extends Serializable {
  def encode(name: String, value: T): Map[String, F]
}
