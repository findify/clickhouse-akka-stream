package io.findify.clickhouse.format

import akka.util.ByteString
import io.findify.clickhouse.format.Field._
import io.findify.clickhouse.format.output.JSONEachRowOutputFormat
import org.scalatest.{FlatSpec, Matchers}

class JSONEachRowOutputFormatTest extends FlatSpec with Matchers {
  val enc = new JSONEachRowOutputFormat()
  it should "encode simple Row" in {
    val row = Row(Map("foo" -> Field.UInt32(1), "bar" -> Field.CString("baz")))
    enc.write(row).utf8String shouldBe ByteString("""{"foo":1,"bar":"baz"}""" + "\n").utf8String
  }

  it should "encode nested objects" in {
    val row = Row(Map("foo" -> Field.UInt32(1), "bar" -> CNested(Seq(ScalarRow(Map("a" -> Int32(1))), ScalarRow(Map("a" -> Int32(2)))))))
    enc.write(row).utf8String shouldBe ByteString("""{"foo":1,"bar.a":[1,2]}""" + "\n").utf8String
  }

  it should "encode arrays" in {
    val row = Row(Map("foo" -> Field.UInt32(1), "bar" -> CArray(Seq(Int32(1), Int32(2)))))
    enc.write(row).utf8String shouldBe ByteString("""{"foo":1,"bar":[1,2]}""" + "\n").utf8String
  }
}
