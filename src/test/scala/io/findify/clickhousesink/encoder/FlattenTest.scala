package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.ClickhouseSink
import io.findify.clickhousesink.field.{NestedTable, Row, SimpleField}
import org.scalatest.{FlatSpec, Matchers}

class FlattenTest extends FlatSpec with Matchers {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  it should "generate value repr for plain classes" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    val tree = encoder.encode(Simple("foo", 7))
    ClickhouseSink.flatten(tree) shouldBe "('foo',7)"
  }

  it should "generate repr for arrays of primitives" in {
    case class Simple(key: String, value: Seq[Int])
    val encoder = deriveEncoder[Simple]
    val tree = encoder.encode(Simple("foo", Seq(7)))
    ClickhouseSink.flatten(tree) shouldBe "('foo',[7])"
  }

  it should "generate repr for nested tables" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    val tree = encoderRoot.encode(Root("key", Seq(Nested("foo", 1), Nested("bar", 2))))
    ClickhouseSink.flatten(tree) shouldBe "('key',['foo','bar'],[1,2])"
  }
}
