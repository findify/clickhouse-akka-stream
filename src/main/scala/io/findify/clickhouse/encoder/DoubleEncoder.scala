package io.findify.clickhouse.encoder

import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field, SimpleField}

class DoubleEncoder extends ScalarEncoder[Double] {
  override def defaultType: String = "Float64"
  override def encodeRaw(value: Double): String = value.toString
}
