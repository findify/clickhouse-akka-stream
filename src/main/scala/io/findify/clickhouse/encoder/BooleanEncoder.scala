package io.findify.clickhouse.encoder

class BooleanEncoder extends ScalarEncoder[Boolean] {
  override def defaultType: String = "UInt8"
  override def encodeRaw(value: Boolean): String = if (value) "1" else "0"
}
