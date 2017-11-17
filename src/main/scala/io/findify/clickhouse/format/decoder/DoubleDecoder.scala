package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.Float64

class DoubleDecoder extends Decoder[Double, Float64] {
  override def decodeValue: PartialFunction[Field, Double] = {
    case Float64(i) => i
  }
}
