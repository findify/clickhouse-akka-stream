package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{ByteNumber, Int8, UInt8}

class BooleanDecoder extends Decoder[Boolean, ByteNumber] {
  override def decodeValue: PartialFunction[Field, Boolean] = {
    case Int8(value) => value == 1
    case UInt8(value) => value == 1
  }
}
