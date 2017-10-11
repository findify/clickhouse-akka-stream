package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.ClickhouseSink
import io.findify.clickhousesink.encoder.generic.deriveEncoder
import org.scalatest.{FlatSpec, Matchers}

class DDLTest extends FlatSpec with Matchers {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._

  it should "build ddl for plain classes" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root")  shouldBe "key String,value Int32"
  }

  it should "build ddl for collections" in {
    case class Simple(key: String, value: Seq[Int])
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root") shouldBe "key String,value Array<Int32>"
  }

  it should "build ddl for nested classes" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.ddl("root") shouldBe "key String,values Nested(foo String,bar Int32)"
  }
}
