package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{ArrayField, NestedTable, Row, SimpleField}
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
}
