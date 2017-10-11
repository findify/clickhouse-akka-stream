package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class LongEncoder extends ScalarEncoder[Long] {
  override def ddl(name: String): String = s"$name Int64"
  override def ddlScalar: String = "Int64"
  override def encode(name: String, value: Long): Seq[Field] = Seq(SimpleField(name, value.toString))
  override def encodeScalar(value: Long): Seq[ScalarField] = Seq(ScalarField(value.toString))
}
