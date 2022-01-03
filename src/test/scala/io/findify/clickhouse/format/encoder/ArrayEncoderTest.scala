package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field.{CArray, CString, Int32, Row}
import org.scalatest.{FlatSpec, Matchers}

class ArrayEncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._

  it should "derive for int arrays" in {
    case class Hello(a: String, b: Seq[Int])
    val enc = deriveEncoder[Hello]
    enc.encode("", Hello("foo", Seq(1, 2, 3))) shouldBe Seq(
      "a" -> CString("foo"),
      "b" -> CArray(Seq(Int32(1), Int32(2), Int32(3)))
    )
  }

  it should "derive for string arrays" in {
    case class Hello(a: String, b: Seq[String])
    val enc = deriveEncoder[Hello]
    enc.encode("", Hello("foo", Seq("x", "y"))) shouldBe Seq(
      "a" -> CString("foo"),
      "b" -> CArray(Seq(CString("x"), CString("y")))
    )
  }
}
