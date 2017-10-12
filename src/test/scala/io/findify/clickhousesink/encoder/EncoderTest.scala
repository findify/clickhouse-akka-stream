package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field._
import org.scalatest.{FlatSpec, Matchers}

class EncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  it should "derive encoder for non-nested classes" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", 7)) shouldBe Seq(SimpleField("'foo'"), SimpleField("7"))
  }

  it should "derive for classes with arrays" in {
    case class Root(key: String, values: Seq[Int])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode(Root("k", Array(1,2))) shouldBe Seq(SimpleField("'k'"), ArrayField(Seq("1", "2")))
  }

  it should "derive for nested classes" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode(Root("key", Seq(Nested("foo", 1)))) shouldBe Seq(SimpleField("'key'"), NestedTable(Seq(Row(Seq(SimpleField("'foo'"), SimpleField("1"))))))
  }

  it should "derive int/long/float/double" in {
    case class Simple(key: String, i: Int, l: Long, f: Float, d: Double)
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", 1, 2L, 3.0f, 4.0)) shouldBe Seq(
      SimpleField("'foo'"),
      SimpleField("1"),
      SimpleField("2"),
      SimpleField("3.0"),
      SimpleField("4.0"),
    )
  }

  it should "derive nullable" in {
    case class Simple(key: String, value: Option[Int])
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", Some(7))) shouldBe Seq(SimpleField("'foo'"), SimpleField("7"))
    encoder.encode(Simple("foo", None)) shouldBe Seq(SimpleField("'foo'"), SimpleField("null"))
  }

}
