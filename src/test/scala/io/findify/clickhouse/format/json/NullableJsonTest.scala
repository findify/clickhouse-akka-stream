package io.findify.clickhouse.format.json

import io.circe.Json
import io.findify.clickhouse.format.Field.{CString, Nullable}
import org.scalatest.{FlatSpec, Matchers}

class NullableJsonTest extends FlatSpec with Matchers {
  it should "skip null values in json" in {
    Nullable(None).valueTuple("foo") shouldBe Nil
  }

  it should "not skip the values on non null" in {
    Nullable(Some(CString("foo"))).valueTuple("bar") shouldBe List("bar" -> Json.fromString("foo"))
  }
}
