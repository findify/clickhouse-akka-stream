package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field.{CString, Int32, Row}
import org.scalatest.{FlatSpec, Matchers}

class AutoEncoderTest extends FlatSpec with Matchers {
  /*import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.row._

  it should "derive encoder for plain case classes" in {
    case class Hello(a: String, b:Int)
    val enc = deriveEncoder[Hello, Row]
    enc.encode("hello", Hello("foo", 1)) shouldBe Map("a" -> CString("foo"), "b" -> Int32(1))
  }*/
}
