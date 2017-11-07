package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field._
import org.scalatest.{FlatSpec, Matchers}

class EncoderTest extends FlatSpec with Matchers {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  it should "derive encoder for non-nested classes" in {
    case class Simple(key: String, value: Int)
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", 7)) shouldBe Seq(SimpleField("'foo'"), SimpleField("7"))
  }

  it should "correctly escape strings" in {
    val en = new StringEncoder()
    en.quote("""foo'""") shouldBe """foo\'"""
    en.quote("""foo\""") shouldBe """foo\\"""
  }

  it should "derive for classes with arrays" in {
    case class Root(key: String, values: Seq[Int])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode(Root("k", Array(1,2))) shouldBe Seq(SimpleField("'k'"), ArrayField(Seq("1", "2")))
  }

  it should "derive for classes with arrays of strings" in {
    case class Root(key: String, values: Seq[String])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode(Root("k", Array("a", "b"))) shouldBe Seq(SimpleField("'k'"), ArrayField(Seq("'a'", "'b'")))
  }

  it should "derive for nested classes" in {
    case class Nested(foo: String, bar: Int)
    case class Root(key: String, values: Seq[Nested])
    val encoderRoot = deriveEncoder[Root]
    encoderRoot.encode(Root("key", Seq(Nested("foo", 1)))) shouldBe Seq(SimpleField("'key'"), NestedTable(Seq(Row(Seq(SimpleField("'foo'"), SimpleField("1"))))))
  }

  it should "derive int/long/float/double" in {
    case class Simple(key: String, i: Int, l: Long, f: Float, d: Double)
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", 1, 2L, 3.0f, 4.0)) shouldBe Seq(
      SimpleField("'foo'"),
      SimpleField("1"),
      SimpleField("2"),
      SimpleField("3.0"),
      SimpleField("4.0"),
    )
  }

  it should "derive nullable" in {
    case class Simple(key: String, value: Option[Int])
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", Some(7))) shouldBe Seq(SimpleField("'foo'"), SimpleField("7"))
    encoder.encode(Simple("foo", None)) shouldBe Seq(SimpleField("'foo'"), SimpleField("null"))
  }

  it should "derive bools" in {
    case class Simple(key: String, value: Boolean)
    val encoder = deriveEncoder[Simple]
    encoder.encode(Simple("foo", true)) shouldBe Seq(SimpleField("'foo'"), SimpleField("1"))
    encoder.encode(Simple("foo", false)) shouldBe Seq(SimpleField("'foo'"), SimpleField("0"))
  }
  
  it should "derive for long objects" in {
    case class PageView(idEvent : String, isProduct : Boolean, productID : Option[String] = None, url : Option[String] = None, ref : Option[String] = None, refMedium : Option[String] = None, refSource : Option[String] = None, refTerm : Option[String] = None, width : Option[Int] = None, height : Option[Int] = None, logging : Boolean, apiKeyGroupId : Int, apiVersion : String, uniqId : Option[String] = None, visitId : Option[String] = None, timeClient : Option[String] = None, timeServer : String, variantItemId : Option[String] = None, apiKey : Option[String] = None)
    val encoder = deriveEncoder[PageView]
  }

  it should "derive for long nested objects" in {
    case class Filter(id: String)
    case class ProductScore(id: String)
    case class Search(idEvent : String, query : Option[String] = None, requestId : Option[String] = None, offset : Int, limit : Int, productIds : Seq[String] = Nil, nbHits : Option[Int] = None, maxScore : Option[Double] = None, duration : Option[Double] = None, noResultFor : Option[String] = None, queryType : Option[String] = None, source : String, isSorted : Boolean, sortKey : Option[String] = None, sortValue : Option[String] = None, filters : Seq[Filter] = Nil, responseTime : Option[Double] = None, noResult : Option[Boolean] = None, correctedQ : Option[String] = None, redirectName : Option[String] = None, redirectUrl : Option[String] = None, slotSC : Option[String] = None, esProductScores : Seq[Double] = Nil, esProductIds : Seq[String] = Nil, productScores : Seq[Double] = Nil, logging : Boolean, apiKeyGroupId : Int, apiVersion : String, uniqId : Option[String] = None, visitId : Option[String] = None, timeClient : Option[String] = None, timeServer : String, esItemScores : Seq[ProductScore] = Nil, shownItemScores : Seq[ProductScore] = Nil, pins : Seq[String] = Nil, apiKey : Option[String] = None)
    implicit val filterEncoder = deriveEncoder[Filter]
    implicit val psEncoder = deriveEncoder[ProductScore]
    val encoder = deriveEncoder[Search]
  }

}
