package io.findify.clickhouse.encoder
import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field, SimpleField}

class IntEncoder extends ScalarEncoder[Int] {
  override def defaultType: String = "Int32"
  override def encodeRaw(value: Int): String = value.toString
}
