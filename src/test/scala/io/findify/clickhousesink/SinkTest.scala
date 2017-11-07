package io.findify.clickhousesink

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import org.scalatest._
import org.testcontainers.containers.wait.Wait


class SinkTest extends TestKit(ActorSystem("test")) with AsyncFlatSpecLike with ForAllTestContainer with ImplicitSender with BeforeAndAfterAll {
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._

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

  case class Foo(k: String, v: Int, opt: Option[String])
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

  case class Nested(n: String)
  case class Root(k: String, v: Seq[Nested])
  implicit val rootEncoder = deriveEncoder[Root]
  it should "create schema for nested objects" in {
    val ddl = rootEncoder.schema("nest", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }
  it should "insert nested data there" in {
    val data = List(Root("a", Seq(Nested("aa"))), Root("b", Nil))
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
    client.query("SELECT count(*) from nest").map(result => assert(result == "1\n"))
  }

  final case class PageView(idEvent : String, isProduct : Boolean, productID : Option[String] = None, url : Option[String] = None, ref : Option[String] = None, refMedium : Option[String] = None, refSource : Option[String] = None, refTerm : Option[String] = None, width : Option[Int] = None, height : Option[Int] = None, logging : Boolean, apiKeyGroupId : Int, apiVersion : String, uniqId : Option[String] = None, visitId : Option[String] = None, timeClient : Option[String] = None, timeServer : String, variantItemId : Option[String] = None, apiKey : Option[String] = None)
  implicit val pageviewEncoder = deriveEncoder[PageView]
  it should "create schema for pageview" in {
    val ddl = pageviewEncoder.schema("pageview", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }
}
