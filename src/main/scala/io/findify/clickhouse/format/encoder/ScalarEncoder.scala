package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.ScalarField

import scala.annotation.implicitNotFound

trait ScalarEncoder[T, F <: ScalarField] extends Encoder[T, F] {
  def encode(name: String, value: T): Seq[(String, F)] = Seq(name -> encode(value))
  def encode(value: T): F
}
