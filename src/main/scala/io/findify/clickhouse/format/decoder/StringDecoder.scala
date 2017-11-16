package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CString, FixedString, Row}

class StringDecoder extends Decoder[String, CString] {
  override def decodeValue: PartialFunction[Field, String] = {
    case CString(value) => value
  }
}
