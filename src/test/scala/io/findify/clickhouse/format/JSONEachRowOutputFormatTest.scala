package io.findify.clickhouse.format

import akka.util.ByteString
import io.findify.clickhouse.format.output.JSONEachRowOutputFormat
import org.scalatest.{FlatSpec, Matchers}

class JSONEachRowOutputFormatTest extends FlatSpec with Matchers {
  val enc = new JSONEachRowOutputFormat()
  it should "encode simple Row" in {
    val row = Row(List(Cell("foo", "UInt32" ,"1"), Cell("bar", "String", "baz")))
    enc.write(row) shouldBe ByteString("""{"foo":"1","bar":"baz"}""" + "\n")
  }
}
