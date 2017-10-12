package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, SimpleField}

class DoubleEncoder extends ScalarEncoder[Double] {
  override def defaultType: String = "Float64"
  override def encodeRaw(value: Double): String = value.toString
}
