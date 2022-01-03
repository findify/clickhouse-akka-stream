package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import io.findify.clickhouse.ClickhouseFlow.Record
import io.findify.clickhouse.format.Field.{CString, Row, UInt32, UInt64}
import io.findify.clickhouse.format.output.JSONEachRowOutputFormat
import org.scalatest._
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.duration._
import scala.concurrent.Await

class SourceSinkTest
    extends TestKit(ActorSystem("test"))
    with AsyncFlatSpecLike
    with ForAllTestContainer
    with ImplicitSender
    with BeforeAndAfterAll {
  import io.findify.clickhouse.format.encoder.generic._
  import io.findify.clickhouse.format.encoder.generic.auto._

  override val container = GenericContainer(
    dockerImage = DockerImage(Left("yandex/clickhouse-server:21.11.6.7")),
    exposedPorts = Seq(8123),
    waitStrategy = Wait.forHttp("/")
  )

  val sv: Supervision.Decider = { case e: Throwable =>
    println("oops", e)
    Supervision.Restart
  }
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(sv)

  implicit val mat = ActorMaterializer(settings)
  lazy val client  = new ClickhouseClient(container.containerIpAddress, container.container.getMappedPort(8123))
  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  it should "make simple queries" in {
    client.execute("SELECT 1").map(result => assert(result == Done))
  }

  case class Foo(k: String, b: Int, ts: Option[String])
  implicit val fooEncoder = deriveEncoder[Foo]

  it should "create table schema for dummy batch insert" in {
    client
      .execute("create table foo (k String, b Int32, ts Nullable(String)) ENGINE = Memory;")
      .map(x => assert(x == Done))
  }

  it should "insert dummy data there" in {
    val data   = Range(1, 10000).map(i => Foo(i.toString, i, None).asRow)
    val source = Source(data)
    val sink = Sink.fromGraph(
      new ClickhouseSink(
        host = container.containerIpAddress,
        port = container.container.getMappedPort(8123),
        table = "foo",
        format = new JSONEachRowOutputFormat()
      )
    )
    val result = source.runWith(sink)
    result.map(r => assert(r == Done))
  }

  it should "have dummy data in db v1" in {
    client
      .query("SELECT count(*) from foo")
      .map(result => assert(result.data.head == Row(Seq("count()" -> UInt64(9999)))))
  }

  it should "have dummy data in db v2" in {
    import io.findify.clickhouse.format.decoder.generic._
    import io.findify.clickhouse.format.decoder.generic.auto._
    implicit val dec = deriveDecoder[Foo]
    client
      .query("SELECT * from foo order by b asc limit 10")
      .map(_.data.map(_.as[Foo](dec)))
      .map(result => assert(result.head == Foo("1", 1, None)))
  }

  it should "create table schema for flow insert" in {
    client.execute("create table flow (k String, b Int32) ENGINE = Memory;").map(x => assert(x == Done))
  }

  it should "insert dummy data via flow" in {
    val data   = Range(1, 10000).map(i => Record(Row(Seq("k" -> CString(i.toString), "b" -> UInt32(i))), i))
    val source = Source(data)
    val flow = Flow.fromGraph(
      ClickhouseFlow[Int](
        host = container.containerIpAddress,
        port = container.container.getMappedPort(8123),
        table = "flow",
        format = new JSONEachRowOutputFormat()
      )
    )
    val result = source.via(flow).runWith(Sink.foreach(x => println(x.passThrough.size)))
    result.map(r => assert(r == Done))
  }

  it should "insert data via periodic flush" in {
    val data   = Range(1, 10000).map(i => Record(Row(Seq("k" -> CString(i.toString), "b" -> UInt32(i))), i))
    val source = Source(data)
    val flow = Flow.fromGraph(
      ClickhouseFlow[Int](
        host = container.containerIpAddress,
        port = container.container.getMappedPort(8123),
        table = "flow",
        format = new JSONEachRowOutputFormat(),
        maxRowsInBuffer = 100000,
        flushInterval = 10.millis
      )
    )
    val result = source.via(flow).runWith(Sink.foreach(x => println(x.passThrough.size)))
    result.map(r => assert(r == Done))
  }

  /*case class Nested(n: String, suffix: Option[Int])
  case class Root(k: String, v: Seq[Nested])
  implicit val rootEncoder = deriveEncoder[Root]
  it should "create schema for nested objects" in {
    val ddl = rootEncoder.schema("nest", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }
  it should "insert nested data there" in {
    val data = List(Root("a", Seq(Nested("aa", Some(1)), Nested("bb", None))), Root("b", Nil))
    val source = Source(data)
    val sink = Sink.fromGraph(new ClickhouseSink[Root](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "nest"
    ))
    val result = source.grouped(10).runWith(sink)
    result.map(r => assert(r == Done))
  }

  it should "have nested data in db" in {
    client.query("SELECT count(*) from nest").map(result => assert(result == "2\n"))
  }

  final case class PageView(idEvent : String, isProduct : Boolean, productID : Option[String] = None, url : Option[String] = None, ref : Option[String] = None, refMedium : Option[String] = None, refSource : Option[String] = None, refTerm : Option[String] = None, width : Option[Int] = None, height : Option[Int] = None, logging : Boolean, apiKeyGroupId : Int, apiVersion : String, uniqId : Option[String] = None, visitId : Option[String] = None, timeClient : Option[String] = None, timeServer : String, variantItemId : Option[String] = None, apiKey : Option[String] = None)
  implicit val pageviewEncoder = deriveEncoder[PageView]
  it should "create schema for pageview" in {
    val ddl = pageviewEncoder.schema("pageview", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }

  it should "map correctly formatted string to DateTime column" in {
    case class Foo(k: String, ts: String)
    val tsEncoder = deriveEncoder[Foo]
    //val ddl = tsEncoder.schema("ts", "ENGINE = Memory")
    Await.result(client.query("CREATE TABLE ts (k String,ts DateTime) ENGINE = Memory"), 10.second)
    val sink = Sink.fromGraph(new ClickhouseSink[Foo](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "ts"
    ))
    val source = Source(List(Foo("a", "2017-01-01 00:00:00")))
    val result = source.grouped(1).runWith(sink)
    result.map(r => assert(r == Done))
  }*/
}
