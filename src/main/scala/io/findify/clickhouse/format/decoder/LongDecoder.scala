package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int64, LongNumber, UInt64}

class LongDecoder extends Decoder[Long, LongNumber] {
  override def decodeValue: PartialFunction[Field, Long] = {
    case UInt64(i) => i
    case Int64(i) => i
  }
}
