package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field._
import org.scalatest.{FlatSpec, Matchers}

class NestedEncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._
  case class Nest(b: String, c: Int)
  case class Hello(a: String, b: Seq[Nest])
  val enc = deriveEncoder[Hello]

  it should "derive for nested rows" in {
    enc.encode("", Hello("a", Seq(Nest("b", 1)))) shouldBe Map("a" -> CString("a"), "b" -> CNested(Seq(ScalarRow(Map("b" -> CString("b"), "c" -> Int32(1))))))
  }

  it should "fail for deeply nested rows" in {
    case class Nest2(b: String, c: Seq[Int])
    case class Hello2(a: String, b: Seq[Nest])
    val enc = deriveEncoder[Hello2]
    enc.encode("", Hello2("a", Nil)) shouldBe "ok"
  }
}
