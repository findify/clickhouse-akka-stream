package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class StringEncoder extends Encoder[String] {
  override def encode(name: String, value: String): Seq[Field] = Seq(SimpleField(name, "String", asString(value)))
  override def encodeScalar(value: String): Seq[ScalarField] = Seq(ScalarField("String", toString))
  override def asString(value: String): String = "'" + value + "'"
}
