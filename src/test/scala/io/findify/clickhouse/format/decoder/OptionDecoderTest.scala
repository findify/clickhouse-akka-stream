package io.findify.clickhouse.format.decoder

import org.scalatest.{FlatSpec, Matchers}

class OptionDecoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.decoder.generic._
  import io.findify.clickhouse.format.decoder.generic.auto._

  it should "decode simple options" in {
    case class Hello(a: Option[Int])
    val dec = deriveDecoder[Hello]
  }

  it should "decode options of non-value classes" in {
    case class Hello(a: Option[String])
    val dec = deriveDecoder[Hello]
  }
}
