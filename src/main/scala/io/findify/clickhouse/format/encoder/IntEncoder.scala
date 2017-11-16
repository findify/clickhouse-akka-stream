package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int32, ScalarField}

class IntEncoder extends ScalarEncoder[Int,Int32] {
  override def encode(value: Int): Int32 = Int32(value)
}
