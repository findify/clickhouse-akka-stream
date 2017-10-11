package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field._
import org.scalatest.{FlatSpec, Matchers}

class EncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  it should "derive encoder for non-nested classes" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    encoder.encode("root",Simple("foo", 7)) shouldBe Seq(SimpleField("key", "String", "'foo'"), SimpleField("value", "Int32", "7"))
  }

  it should "derive for classes with arrays" in {
    case class Root(key: String, values: Seq[Int])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode("root", Root("k", Array(1,2))) shouldBe Seq(SimpleField("key", "String", "'k'"), ArrayField("values", Seq(ScalarField("Int32","1"), ScalarField("Int32","2"))))
  }

  it should "derive for nested classes" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode("root",Root("key", Seq(Nested("foo", 1)))) shouldBe Seq(SimpleField("key","String","'key'"), NestedTable("values",Seq(Row(Seq(SimpleField("foo", "String","'foo'"), SimpleField("bar", "Int32","1"))))))
  }

  it should "derive int/long/float/double" in {
    case class Simple(key: String, i: Int, l: Long, f: Float, d: Double)
    val encoder = deriveEncoder[Simple]
    encoder.encode("root",Simple("foo", 1, 2L, 3.0f, 4.0)) shouldBe Seq(
      SimpleField("key", "String", "'foo'"),
      SimpleField("i", "Int32", "1"),
      SimpleField("l", "Int64", "2"),
      SimpleField("f", "Float32", "3.0"),
      SimpleField("d", "Float64", "4.0"),
    )

  }
}
