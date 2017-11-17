package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field.{CArray, CString, Int32, Row}
import org.scalatest.{FlatSpec, Matchers}

class ArrayDecoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.decoder.generic._
  import io.findify.clickhouse.format.decoder.generic.auto._

  it should "derive decoder for classes with arrays" in {
    case class Simple(a: String, b: Seq[Int])
    val data = Row(Map("a" -> CString("a"), "b" -> CArray(Seq(Int32(1)))))
    val dec = deriveDecoder[Simple]
    dec.decode("", data) shouldBe Simple("a", Seq(1))
  }

  it should "derive decoder for arrays of strings" in {
    case class Simple(a: String, b: Seq[String])
    val data = Row(Map("a" -> CString("a"), "b" -> CArray(Seq(CString("c")))))
    val dec = deriveDecoder[Simple]
    dec.decode("", data) shouldBe Simple("a", Seq("c"))
  }
}
