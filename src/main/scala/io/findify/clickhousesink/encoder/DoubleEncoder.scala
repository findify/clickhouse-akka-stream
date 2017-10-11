package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class DoubleEncoder extends Encoder[Double] {
  override def encode(name: String, value: Double): Seq[Field] = Seq(SimpleField(name, "Float64", value.toString))
  override def encodeScalar(value: Double): Seq[ScalarField] = Seq(ScalarField("Float64", value.toString))
}
