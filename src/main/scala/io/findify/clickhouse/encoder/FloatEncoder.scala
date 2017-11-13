package io.findify.clickhouse.encoder

import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field, SimpleField}

class FloatEncoder extends ScalarEncoder[Float] {
  override def defaultType: String = "Float32"
  override def encodeRaw(value: Float): String = value.toString
}
