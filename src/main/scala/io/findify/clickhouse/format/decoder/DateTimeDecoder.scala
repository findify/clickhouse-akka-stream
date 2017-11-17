package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.CDateTime
import org.joda.time.LocalDateTime

class DateTimeDecoder extends Decoder[LocalDateTime, CDateTime] {
  override def decodeValue: PartialFunction[Field, LocalDateTime] = {
    case CDateTime(value) => value
  }
}
