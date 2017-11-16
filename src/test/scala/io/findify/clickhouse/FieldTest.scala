package io.findify.clickhouse

import io.circe.Json
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.FixedString
import org.scalatest.{FlatSpec, Matchers}

class FieldTest extends FlatSpec with Matchers {
  it should "parse fixedstring" in {
    Field("FixedString(16)", Json.fromString("bar")) shouldBe FixedString("bar", 16)
  }
}
