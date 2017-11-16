package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int64, ScalarField}

class LongEncoder extends ScalarEncoder[Long, Int64] {
  override def encode(value: Long): Int64 = Int64(value)
}
