package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Float64, ScalarField}

class DoubleEncoder extends ScalarEncoder[Double] {
  override def encode(value: Double): ScalarField = Float64(value)
}
