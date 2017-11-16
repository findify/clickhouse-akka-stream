package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CDateTime, ScalarField}
import org.joda.time.{LocalDate, LocalDateTime}

class DateTimeEncoder extends ScalarEncoder[LocalDateTime] {
  override def encode(value: LocalDateTime): ScalarField = CDateTime(value)
}
