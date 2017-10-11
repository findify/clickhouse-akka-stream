package io.findify.clickhousesink

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
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
  implicit val mat = ActorMaterializer()
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
    val ddl = fooEncoder.ddl("foo", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }

  it should "insert dummy data there" in {
    val data = List(Foo("a", 1, None), Foo("b", 2, Some("b")), Foo("c", 3, Some("c")))
    val source = Source(data)
    val sink = Sink.fromGraph(new ClickhouseSink[Foo](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "foo"
    ))
    val result = source.runWith(sink)
    result.map(r => assert(r == Done))
  }

  it should "have dummy data in db" in {
    client.query("SELECT count(*) from foo").map(result => assert(result == "3\n"))
  }

  case class Nested(n: String)
  case class Root(k: String, v: Seq[Nested])
  implicit val rootEncoder = deriveEncoder[Root]
  it should "create schema for nested objects" in {
    val ddl = rootEncoder.ddl("nest", "ENGINE = Memory")
    client.query(ddl).map(x => assert(x == ""))
  }
  it should "insert nested data there" in {
    val data = List(Root("a", Seq(Nested("aa"))))
    val source = Source(data)
    val sink = Sink.fromGraph(new ClickhouseSink[Root](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "nest"
    ))
    val result = source.runWith(sink)
    result.map(r => assert(r == Done))
  }

  it should "have nested data in db" in {
    client.query("SELECT count(*) from nest").map(result => assert(result == "1\n"))
  }

}
