package io.findify.clickhouse.encoder

import io.findify.clickhouse.ClickhouseSink
import io.findify.clickhouse.field.{NestedTable, Row, SimpleField}
import org.scalatest.{FlatSpec, Matchers}

class FlattenTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.encoder.generic._
  import io.findify.clickhouse.encoder.generic.auto._
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

  it should "generate repr for empty nested tables" in {
    case class Nested(foo: String, bar: Int, baz: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    val tree = encoderRoot.encode(Root("key", Nil))
    ClickhouseSink.flatten(tree) shouldBe "('key',[],[],[])"
  }

  it should "generate repr for nested tables with nullable fields" in {
    case class Nested(foo: String, bar: Option[Int], bar2: Option[Int], bar3: Option[Int])
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    val tree = encoderRoot.encode(Root("key", Seq(Nested("a", None,None,None), Nested("b", None, None, None))))
    ClickhouseSink.flatten(tree) shouldBe "('key',['a','b'],[null,null],[null,null],[null,null])"
  }
}
