package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.{ClickhouseSink, CustomMapper}
import io.findify.clickhousesink.encoder.generic.deriveEncoder
import io.findify.clickhousesink.field.SimpleField
import org.joda.time.{DateTime, LocalDate}
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
    encoder.ddl("root") shouldBe "key String,value Array(Int32)"
  }

  it should "build ddl for nested classes" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.ddl("root") shouldBe "key String,values Nested(foo String,bar Int32)"
  }

  it should "work for nullable fields" in {
    case class Simple(key: String, value: Option[Int])
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root") shouldBe "key String,value Nullable(Int32)"
  }

  it should "do custom mapping for field names" in {
    case class Simple(key: String, value: Int)
    val uppercaseMapper = new CustomMapper {
      override def fieldName(name: String): String = name.toUpperCase
      override def fieldType(name: String, currentType: String): String = currentType
    }
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root", uppercaseMapper) shouldBe "KEY String,VALUE Int32"
  }

  it should "do custom mapping for field types" in {
    case class Simple(key: String, value: Int)
    val unsignedMapper = new CustomMapper {
      override def fieldName(name: String): String = name
      override def fieldType(name: String, currentType: String): String = currentType match {
        case "Int32" => "UInt32"
        case other => other
      }
    }
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root", unsignedMapper) shouldBe "key String,value UInt32"
  }

  it should "support custom encoders for scalars" in {
    case class Color(r: Byte, g: Byte, b: Byte)
    case class Simple(key: String, custom: Color)
    implicit val colorEncoder = new ScalarEncoder[Color] {
      override def defaultType: String = "String"
      override def encodeRaw(value: Color): String = s"'r=${value.r},g=${value.g},b=${value.b}'"
    }
    val encoder = deriveEncoder[Simple]
    encoder.schema("simple", "ENGINE = Memory") shouldBe "CREATE TABLE simple (key String,custom String) ENGINE = Memory"
    encoder.encode(Simple("foo", Color(1,2,3))) shouldBe Seq(SimpleField("'foo'"), SimpleField("'r=1,g=2,b=3'"))
  }

  it should "work with separators" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    encoder.ddl("root", separator = ", ")  shouldBe "key String, value Int32"
  }

  it should "deal with datetime and date" in {
    case class Simple(key: String, ts: DateTime, day: LocalDate)
    val encoder = deriveEncoder[Simple]
    encoder.schema("simple", "ENGINE = Memory") shouldBe "CREATE TABLE simple (key String,ts DateTime,day Date) ENGINE = Memory"
    encoder.encode(Simple("foo", new DateTime(2017, 1, 1, 0, 0, 1), new LocalDate(2017, 1, 1))) shouldBe Seq(SimpleField("'foo'"), SimpleField("'2017-01-01 00:00:01'"), SimpleField("'2017-01-01'"))
  }
}
