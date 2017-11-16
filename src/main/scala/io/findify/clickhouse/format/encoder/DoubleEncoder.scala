package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Float64, ScalarField}

class DoubleEncoder extends ScalarEncoder[Double, Float64] {
  override def encode(value: Double): Float64 = Float64(value)
}
