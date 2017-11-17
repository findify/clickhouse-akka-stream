package io.findify.clickhouse.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.findify.clickhouse.ClickhouseSink
import io.findify.clickhouse.format.encoder.generic._
import io.findify.clickhouse.format.encoder.generic.auto._


class WriteExample {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("clickhouse")
    implicit val mat = ActorMaterializer()

    case class Simple(key: String, value: Int)
    implicit val encoder = deriveEncoder[Simple]
    // ddl:
    // create table simple(key String, value: Int32) ENGINE = Memory
    val source = Source(List(Simple("a", 1), Simple("b", 2), Simple("c", 3)))
      .map(_.asRow)
      .grouped(100)
    val sink = Sink.fromGraph(new ClickhouseSink(
      host = "localhost",
      port = 8123,
      table = "simple"
    ))
    val result = source.runWith(sink)
    // will perform:
    // INSERT INTO simple FORMAT JSONEachRow
    // {"key": "a", "value": 1}
    // {"key": "b", "value": 2}
    // {"key": "c", "value": 3}
  }
}
