package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, SimpleField}

class FloatEncoder extends ScalarEncoder[Float] {
  override def defaultType: String = "Float32"
  override def encodeRaw(value: Float): String = value.toString
}
