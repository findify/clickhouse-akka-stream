package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int32, IntNumber, UInt32}

class IntDecoder extends Decoder[Int, IntNumber] {
  override def decodeValue: PartialFunction[Field, Int] = {
    case Int32(i) => i
    case UInt32(i) => i
  }
}