package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{ScalarField, UInt8}


class BooleanEncoder extends ScalarEncoder[Boolean, UInt8] {
  override def encode(value: Boolean): UInt8 = UInt8(if (value) 1 else 0)
}
