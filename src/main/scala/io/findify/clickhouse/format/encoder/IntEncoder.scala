package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int32, ScalarField}

class IntEncoder extends ScalarEncoder[Int] {
  override def encode(value: Int): ScalarField = Int32(value)
}
