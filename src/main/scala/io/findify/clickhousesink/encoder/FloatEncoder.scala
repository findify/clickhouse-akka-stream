package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class FloatEncoder extends ScalarEncoder[Float] {
  override def encode(name: String, value: Float): Seq[Field] = Seq(SimpleField(name, "Float32", value.toString))
  override def encodeScalar(value: Float): Seq[ScalarField] = Seq(ScalarField("Float32", value.toString))

}
