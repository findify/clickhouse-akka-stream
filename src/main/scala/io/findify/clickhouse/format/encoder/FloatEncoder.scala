package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Float32, ScalarField}

class FloatEncoder extends ScalarEncoder[Float] {
  override def encode(value: Float): ScalarField = Float32(value)
}
