package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.ScalarField

trait Encoder[T] {
  def encode(name: String, value: T): Map[String, Field] = encodeS(name, value)
  def encodeS(name: String, value: T): Map[String, ScalarField]
}
