package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

class IntEncoder extends ScalarEncoder[Int] {
  override def defaultType: String = "Int32"
  override def encodeRaw(value: Int): String = value.toString
}
