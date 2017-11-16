package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int64, ScalarField}

class LongEncoder extends ScalarEncoder[Long] {
  override def encode(value: Long): ScalarField = Int64(value)
}
