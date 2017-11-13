package io.findify.clickhouse.encoder

import java.time.format.DateTimeFormatter

import org.joda.time.format.DateTimePrinter

object DateEncoder {
  private val jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")
  private val javaFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
  class JodaLocalDateEncoder extends ScalarEncoder[org.joda.time.LocalDate] {
    override def defaultType: String = "Date"
    override def encodeRaw(value: org.joda.time.LocalDate): String = s"'${value.toString(jodaFormatter)}'"
  }
  class JavaDateEncoder extends ScalarEncoder[java.time.LocalDate] {
    override def defaultType: String = "DateTime"
    override def encodeRaw(value: java.time.LocalDate): String = s"'${value.format(javaFormatter)}'"
  }

}
