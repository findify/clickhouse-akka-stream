package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field.{CString, Int32, Row}
import org.joda.time.{LocalDate, LocalDateTime}
import org.scalatest.{FlatSpec, Matchers}

class AutoDecoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.decoder.generic._
  import io.findify.clickhouse.format.decoder.generic.auto._
  it should "derive decoder for simple case classes" in {
    case class Simple(a: String, b: Int)
    val data = Row(Map("a" -> CString("a"), "b" -> Int32(1)))
    val dec = deriveDecoder[Simple]
    dec.decode("", data) shouldBe Simple("a", 1)
  }
  it should "derive for rich objects" in {
    case class RichBitch(a: Byte, b: Boolean, c: Seq[Byte], d: LocalDate, e: LocalDateTime, f: Seq[LocalDate], g: Long, h: Double, i: Float)
    val dec = deriveDecoder[RichBitch]
  }
}
