package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Float32, ScalarField}

class FloatEncoder extends ScalarEncoder[Float, Float32] {
  override def encode(value: Float): Float32 = Float32(value)
}
