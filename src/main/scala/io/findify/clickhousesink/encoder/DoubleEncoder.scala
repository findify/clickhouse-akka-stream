package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class DoubleEncoder extends ScalarEncoder[Double] {
  override def ddl(name: String): String = s"$name Float64"
  override def ddlScalar: String = "Float64"
  override def encode(name: String, value: Double): Seq[Field] = Seq(SimpleField(name, value.toString))
  override def encodeScalar(value: Double): Seq[ScalarField] = Seq(ScalarField(value.toString))
}
