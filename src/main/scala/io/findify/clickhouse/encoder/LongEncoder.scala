package io.findify.clickhouse.encoder
import io.findify.clickhouse.field.{Field, SimpleField}

class LongEncoder extends ScalarEncoder[Long] {
  override def defaultType: String = "Int64"
  override def encodeRaw(value: Long): String = value.toString
}
