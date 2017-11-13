package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import org.scalatest._
import org.testcontainers.containers.wait.Wait
import scala.concurrent.duration._
import scala.concurrent.Await


class SinkTest extends TestKit(ActorSystem("test")) with AsyncFlatSpecLike with ForAllTestContainer with ImplicitSender with BeforeAndAfterAll {
  import io.findify.clickhouse.encoder.generic._
  import io.findify.clickhouse.encoder.generic.auto._

  override val container = GenericContainer(
    imageName = "yandex/clickhouse-server:1.1.54292",
    exposedPorts = Seq(8123),
    waitStrategy = Wait.forHttp("/")
  )

  lazy val client = new Client(container.containerIpAddress, container.container.getMappedPort(8123))
  val sv: Supervision.Decider = {
    case e: Throwable =>
      println("oops", e)
      Supervision.Restart
  }
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(sv)

  implicit val mat = ActorMaterializer(settings)
  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  it should "make simple queries" in {
    client.query("SELECT 1").map(result => assert(result == "1\n"))
  }

  case class Foo(k: String, b: Int, ts: Option[String])
  implicit val fooEncoder = deriveEncoder[Foo]
  it should "create table schema for dummy batch insert" in {
    val ddl = fooEncoder.schema("foo", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }

  it should "insert dummy data there" in {
    val data = Range(1,10000).map(i => Foo(i.toString, i, None))
    val source = Source(data)
    val sink = Sink.fromGraph(new ClickhouseSink[Foo](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "foo"
    ))
    val result = source.grouped(100).runWith(sink)
    result.map(r => assert(r == Done))
  }

  it should "have dummy data in db" in {
    client.query("SELECT count(*) from foo").map(result => assert(result == "9999\n"))
  }

  case class Nested(n: String, suffix: Option[Int])
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
  }
}
