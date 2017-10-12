package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class LongEncoder extends ScalarEncoder[Long] {
  override def defaultType: String = "Int64"
  override def encodeRaw(value: Long): String = value.toString
}
