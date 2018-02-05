package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field._

class IntDecoder extends Decoder[Int, IntNumber] {
  override def decodeValue: PartialFunction[Field, Int] = {
    case UInt8(i) => i
    case Int16(i) => i
    case UInt16(i) => i
    case Int32(i) => i
  }
}