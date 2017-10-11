package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class IntEncoder extends ScalarEncoder[Int] {
  override def encode(name: String, value: Int): Seq[Field] = Seq(SimpleField(name, "Int32", value.toString))
  override def encodeScalar(value: Int): Seq[ScalarField] = Seq(ScalarField("Int32", value.toString))
}
