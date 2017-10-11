package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class StringEncoder extends ScalarEncoder[String] {
  override def ddl(name: String): String = s"$name String"
  override def ddlScalar: String = "String"
  override def encode(name: String, value: String): Seq[Field] = Seq(SimpleField(name, "'" + value + "'"))
  override def encodeScalar(value: String): Seq[ScalarField] = Seq(ScalarField(value))
}
