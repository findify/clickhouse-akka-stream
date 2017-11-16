package io.findify.clickhouse.format

import akka.util.ByteString
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.output.JSONEachRowOutputFormat
import org.scalatest.{FlatSpec, Matchers}

class JSONEachRowOutputFormatTest extends FlatSpec with Matchers {
  val enc = new JSONEachRowOutputFormat()
  it should "encode simple Row" in {
    val row = Row(Map("foo" -> Field.UInt32(1), "bar" -> Field.CString("baz")))
    enc.write(row).utf8String shouldBe ByteString("""{"foo":1,"bar":"baz"}""" + "\n").utf8String
  }
}
