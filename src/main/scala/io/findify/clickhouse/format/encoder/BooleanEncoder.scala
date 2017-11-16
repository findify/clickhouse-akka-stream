package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{ScalarField, UInt8}


class BooleanEncoder extends ScalarEncoder[Boolean] {
  override def encode(value: Boolean): ScalarField = UInt8(if (value) 1 else 0)
}
