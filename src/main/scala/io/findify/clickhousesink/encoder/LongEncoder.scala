package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class LongEncoder extends ScalarEncoder[Long] {
  override def encode(name: String, value: Long): Seq[Field] = Seq(SimpleField(name, "Int64", value.toString))
  override def encodeScalar(value: Long): Seq[ScalarField] = Seq(ScalarField("Int64", value.toString))
}
