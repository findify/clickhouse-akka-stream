package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class IntEncoder extends ScalarEncoder[Int] {
  override def ddl(name: String): String = s"$name Int32"
  override def ddlScalar: String = "Int32"
  override def encode(name: String, value: Int): Seq[Field] = Seq(SimpleField(name, value.toString))
  override def encodeScalar(value: Int): Seq[ScalarField] = Seq(ScalarField(value.toString))
}
