package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class StringEncoder extends ScalarEncoder[String] {
  override def defaultType: String = "String"
  override protected def encodeRaw(value: String): String = "'" + value + "'"
}
