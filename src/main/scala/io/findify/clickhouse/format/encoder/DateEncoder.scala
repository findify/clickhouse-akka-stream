package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CDate, ScalarField}
import org.joda.time.LocalDate

class DateEncoder extends ScalarEncoder[LocalDate, CDate] {
  override def encode(value: LocalDate): CDate = CDate(value)
}
