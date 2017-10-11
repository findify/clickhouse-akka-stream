package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.ScalarField

trait ScalarEncoder[T] extends Encoder[T] {
  def encodeScalar(value: T): Seq[ScalarField]
  def ddlScalar: String
}
