package io.findify.clickhouse.format.encoder

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import io.findify.clickhouse.format.Field.{CString, Int32, Row}
import org.scalatest.{FlatSpec, Matchers}

class AutoEncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._

  it should "derive encoder for plain case classes" in {
    case class Hello(a: String, b: Int)
    val enc = deriveEncoder[Hello]
    enc.encode("hello", Hello("foo", 1)) shouldBe Seq("a" -> CString("foo"), "b" -> Int32(1))
  }

  it should "be serializable" in {
    val bytes  = new ByteArrayOutputStream()
    val stream = new ObjectOutputStream(bytes)
    stream.writeObject(AutoEncoderTest.enc)
    stream.flush()
    bytes.size() should be > (1700)
  }
}

object AutoEncoderTest {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._
  case class Hello(a: String, b: Int)
  val enc = deriveEncoder[Hello]

}
