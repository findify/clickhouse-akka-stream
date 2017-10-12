package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class StringEncoder extends ScalarEncoder[String] {
  override def defaultType: String = "String"
  override def encodeRaw(value: String): String = "'" + value + "'"
}
