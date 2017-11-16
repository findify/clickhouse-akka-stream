package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.ScalarField

trait ScalarEncoder[T] extends Encoder[T] {
  //def encode(name: String, value: T): Map[String, Field] = Map(name -> encode(value))
  def encodeS(name: String, value: T): Map[String, ScalarField] = Map(name -> encode(value))
  def encode(value: T): ScalarField
}
