package io.findify.clickhouse.format.encoder

import org.scalatest.{FlatSpec, Matchers}

class MagnoliaEncoderTest extends FlatSpec with Matchers {

  import magnolia.auto._
  import generic._

  it should "derive simple typeclass" in {
    case class Hello(foo: String, bar: Option[Int])
    val encoder = gen[Hello]
    val result = encoder.encode("hi", Hello("wow", Some(7)))
    val br=1
  }

}
