package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Int32, UInt32}

object IntDecoder {
  class Int32Decoder extends Decoder[Int, Int32] {
    override def decodeValue: PartialFunction[Field, Int] = {
      case Int32(i) => i
    }
  }
  class UInt32Decoder extends Decoder[Int, UInt32] {
    override def decodeValue: PartialFunction[Field, Int] = {
      case UInt32(i) => i
    }
  }
}
