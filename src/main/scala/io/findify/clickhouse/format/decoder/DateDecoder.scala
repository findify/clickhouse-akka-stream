package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.CDate
import org.joda.time.LocalDate

class DateDecoder extends Decoder[LocalDate, CDate] {
  override def decodeValue: PartialFunction[Field, LocalDate] = {
    case CDate(value) => value
  }
}
