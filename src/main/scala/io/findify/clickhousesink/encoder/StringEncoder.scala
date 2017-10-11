package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class StringEncoder extends Encoder[String] {
  override def encode(value: String): Seq[Field] = Seq(SimpleField(asString(value)))
  override def asString(value: String): String = "'" + value + "'"
}
