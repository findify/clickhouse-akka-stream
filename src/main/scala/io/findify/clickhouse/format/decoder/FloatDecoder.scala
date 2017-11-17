package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.Float32

class FloatDecoder extends Decoder[Float, Float32] {
  override def decodeValue: PartialFunction[Field, Float] = {
    case Float32(i) => i
  }
}
