package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class LongEncoder extends ScalarEncoder[Long] {
  override def defaultType: String = "Int64"
  override def encodeRaw(value: Long): String = value.toString
}
