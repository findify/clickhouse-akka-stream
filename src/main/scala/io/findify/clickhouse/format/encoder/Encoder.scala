package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field

import scala.annotation.implicitNotFound

trait Encoder[T, +F <: Field] {
  def encode(name: String, value: T): Map[String, F]
}
