package io.findify.clickhouse.format.encoder

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import io.findify.clickhouse.format.encoder.magnolia.auto.gen
import org.scalatest.{FlatSpec, Matchers}

class MagnoliaEncoderTest extends FlatSpec with Matchers {
  import generic._
  import magnolia.auto._


  it should "derive simple typeclass" in {
    case class Hello(foo: String, bar: Option[Int])
    val encoder = gen[Hello]
    val result = encoder.encode("hi", Hello("wow", Some(7)))
    val br=1
  }

  it should "be serializable" in {
    val bytes = new ByteArrayOutputStream()
    val stream = new ObjectOutputStream(bytes)
    stream.writeObject(MagnoliaEncoderTest.encoder)
    stream.flush()
    bytes.size() should be > (10)
  }
}

object MagnoliaEncoderTest {
  import magnolia.auto.gen
  import generic._
  case class Hello(foo: String, bar: Option[Int])
  val encoder = gen[Hello]

}