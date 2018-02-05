package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.testkit.TestKit
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field._
import io.findify.clickhouse.format.output.JSONEachRowOutputFormat
import org.joda.time.{LocalDate, LocalDateTime}
import org.scalacheck.Test.Parameters
import org.scalacheck.{Gen, Prop}
import org.scalatest.prop.{Checkers, PropertyChecks}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike, Matchers}
import org.testcontainers.containers.wait.Wait

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Random

class FuzzyWriteReadTest extends TestKit(ActorSystem("test")) with FlatSpecLike with Matchers with PropertyChecks with Checkers with ForAllTestContainer with BeforeAndAfterAll with LazyLogging {
  override val container = GenericContainer(
    imageName = "yandex/clickhouse-server:1.1.54342",
    exposedPorts = Seq(8123),
    waitStrategy = Wait.forHttp("/")
  )
  val sv: Supervision.Decider = {
    case e: Throwable =>
      println("oops", e)
      Supervision.Restart
  }
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(sv)

  implicit val mat = ActorMaterializer(settings)
  lazy val client = new ClickhouseClient(container.containerIpAddress, container.container.getMappedPort(8123))
  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  case class FieldType(top: Option[String], base: String) {
    def asDDL = top match {
      case None => base
      case Some(other) => s"$other($base)"
    }
  }
  case class Schema(table: String, key: FieldType, value: FieldType, records: List[Row]) {
    def ddl = s"create table $table (k ${key.asDDL}, v ${value.asDDL}) ENGINE Memory"

  }
  val keyTypes = Seq("String", "Date", "DateTime",
    "UInt8", "UInt16", "UInt32", "UInt64", "Int8", "Int16", "Int32", "Int64"
  )
  val numtypes = Seq("Float32", "Float64")
  val compoundTypes = Seq(Some("Nullable"), Some("Array"), None)
  val types = for {
    top <- compoundTypes
    basic <- keyTypes ++ numtypes
  } yield {
    FieldType(top, basic)
  }

  def genDate: Gen[CDate] = for {
    year <- Gen.choose(1971, 2017)
    month <- Gen.choose(1, 12)
    day <- Gen.choose(1, 28)
  } yield {
    CDate(new LocalDate(year, month, day))
  }
  def genDateTime: Gen[CDateTime] = for {
    date <- genDate
    hour <- Gen.choose(0, 23)
    min <- Gen.choose(0, 59)
    sec <- Gen.choose(0, 59)
  } yield {
    CDateTime(new LocalDateTime(date.raw.getYear, date.raw.getMonthOfYear, date.raw.getDayOfMonth, hour, min, sec))
  }
  def genField(field: FieldType): Gen[Field] = {
    val scalar: Gen[ScalarField] = field.base match {
      case "String" => Gen.alphaNumStr.map(CString.apply)
      case "Date" => genDate
      case "DateTime" => genDateTime
      case "Int8" => Gen.choose(Byte.MinValue, Byte.MaxValue).map(x => Int8(x))
      case "UInt8" => Gen.choose(0, 255).map(x => UInt8(x.shortValue()))
      case "Int16" => Gen.choose(Short.MinValue, Short.MaxValue).map(x => Int16(x.intValue()))
      case "UInt16" => Gen.choose(0, 65535).map(x => UInt16(x))
      case "Int32" => Gen.choose(Int.MinValue, Int.MaxValue).map(Int32.apply)
      case "UInt32" => Gen.choose(0L, 4294967295L).map(UInt32.apply)
      case "Int64" => Gen.choose(Long.MinValue, Long.MaxValue).map(Int64.apply)
      case "UInt64" => Gen.choose(0, Long.MaxValue).map(UInt64.apply) // kek
      case "Float32" => Gen.choose(Float.MinValue, Float.MaxValue).map(Float32.apply)
      case "Float64" => Gen.choose(Double.MinValue, Double.MaxValue).map(Float64.apply)
    }
    field.top match {
      case None =>
        scalar
      case Some("Array") => for {
        elements <- Gen.choose(1,10)
        field <- Gen.listOfN(elements, scalar)
      } yield {
        CArray(field)
      }
      case Some("Nullable") => for {
        isnull <- Gen.oneOf(true, false)
        field <- scalar
      } yield {
        if (isnull) Nullable(None) else Nullable(Some(field))
      }
    }
  }

  val schemas = for {
    table <- Gen.choose(1,100000)
    key <- Gen.oneOf(keyTypes).map(FieldType(None, _))
    value <- Gen.oneOf(types)
    keyFields <- Gen.listOfN(10, genField(key))
    valueFields <- Gen.listOfN(10, genField(value))
  } yield {
    val rows = keyFields.zip(valueFields).map {
      case (k, v) => Row(Map("k" -> k, "v" -> v))
    }
    Schema(s"test$table", key, value, rows)
  }
  implicit val params: Parameters = Parameters.default.withMinSuccessfulTests(1)
  it should "generate schemas, put&read data" in check( {
    Prop.forAll(schemas) { case s @ Schema(table, _, _, rows) => {
      val sink = Sink.fromGraph(new ClickhouseSink(
        host = container.containerIpAddress,
        port = container.container.getMappedPort(8123),
        table = table,
        format = new JSONEachRowOutputFormat()
      ))
      logger.info(s.ddl)
      val ddl = s.ddl
      val schemaOK = Await.result(client.execute(ddl), 10.seconds) == Done
      val insertOK = Await.result(Source(rows).runWith(sink), 10.seconds) == Done
      val read = Await.result(client.query(s"select * from $table"), 10.seconds)
      val readOK = read.data == rows
      val ok = schemaOK && insertOK && readOK
      if (!ok) {
        val br=1
      }
      ok
    }}
  }, MinSuccessful(100))
}
