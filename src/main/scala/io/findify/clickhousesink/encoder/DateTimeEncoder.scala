package io.findify.clickhousesink.encoder

import java.time.format.DateTimeFormatter

object DateTimeEncoder {
  private val jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  private val javaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  class JodaLocalDateTimeEncoder extends ScalarEncoder[org.joda.time.LocalDateTime] {
    override def defaultType: String = "DateTime"
    override def encodeRaw(value: org.joda.time.LocalDateTime): String = s"'${value.toString(jodaFormatter)}'"
  }
  class JodaDateTimeEncoder extends ScalarEncoder[org.joda.time.DateTime] {
    override def defaultType: String = "DateTime"
    override def encodeRaw(value: org.joda.time.DateTime): String = s"'${value.toString(jodaFormatter)}'"
  }
  class JavaDateTimeEncoder extends ScalarEncoder[java.time.LocalDateTime] {
    override def defaultType: String = "DateTime"
    override def encodeRaw(value: java.time.LocalDateTime): String = s"'${value.format(javaFormatter)}'"
  }
}
