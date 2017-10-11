package io.findify.clickhousesink

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import org.scalatest._
import org.testcontainers.containers.wait.Wait

import scala.concurrent.{Await, Future}
import io.circe.generic.semiauto._

class SinkTest extends TestKit(ActorSystem("test")) with AsyncFlatSpecLike with ForAllTestContainer with ImplicitSender with BeforeAndAfterAll {
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

  /*it should "create table schema for dummy batch insert" in {
    val schema = "CREATE TABLE foo (k String, v Int32) ENGINE = Memory"
    client.query(schema).map(x => assert(x == ""))
  }

  it should "insert dummy data there" in {
    case class Foo(k: String, v: Int)
    val data = List(Foo("a", 1), Foo("b", 2), Foo("c", 3))
    implicit val encoder = deriveEncoder[Foo]
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
  }*/

  it should "create schema for nested objects" in {
    val schema = "CREATE TABLE nest (k String, v Nested(n String)) ENGINE = Memory"
    client.query(schema).map(x => assert(x == ""))
  }
  it should "insert nested data there" in {
    case class Nested(n: String)
    case class Root(k: String, v: Seq[Nested])
    val data = List(Root("a", Seq(Nested("aa"))))
    implicit val nestEncoder = deriveEncoder[Nested]
    implicit val rootEncoder = deriveEncoder[Root]
    val source = Source(data)
    val sink = Sink.fromGraph(new ClickhouseSink[Root](
      host = container.containerIpAddress,
      port = container.container.getMappedPort(8123),
      table = "nest"
    ))
    val result = source.runWith(sink)
    result.map(r => assert(r == Done))
  }


}
