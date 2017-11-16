package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CString, Int32, Nullable, Row}
import org.scalatest.{FlatSpec, Matchers}

class OptionEncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._
  case class Hello(a: String, b: Option[Int])
  val enc = deriveEncoder[Hello]

  it should "derive encoder for options" in {
    enc.encode("hello", Hello("foo", Some(1))) shouldBe Map("a" -> CString("foo"), "b" -> Nullable(Some(Int32(1))))
  }

  it should "work with none" in {
    enc.encode("hello", Hello("foo", None)) shouldBe Map("a" -> CString("foo"), "b" -> Nullable(None))
  }

  it should "derive plain option encoders for string" in {
    val enc = deriveEncoder[Option[String]]
    enc.encode("f", Some("x")) shouldBe Map("f" -> Nullable(Some(CString("x"))))
  }

  it should "derive plain option encoders for int" in {
    val enc = deriveEncoder[Option[Int]]
    enc.encode("f", Some(1)) shouldBe Map("f" -> Nullable(Some(Int32(1))))
  }
}
