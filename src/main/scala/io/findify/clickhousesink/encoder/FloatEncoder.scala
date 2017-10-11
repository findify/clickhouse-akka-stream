package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class FloatEncoder extends ScalarEncoder[Float] {
  override def ddl(name: String): String = s"$name Float32"
  override def ddlScalar: String = "Float32"
  override def encode(name: String, value: Float): Seq[Field] = Seq(SimpleField(name, value.toString))
  override def encodeScalar(value: Float): Seq[ScalarField] = Seq(ScalarField(value.toString))

}
